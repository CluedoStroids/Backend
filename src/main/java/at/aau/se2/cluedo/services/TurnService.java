package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gamemanager.GameState;
import at.aau.se2.cluedo.models.gameobjects.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TurnService {
    private static final Logger logger = LoggerFactory.getLogger(TurnService.class);

    @Autowired
    private GameService gameService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Track turn states for each lobby
    private final Map<String, TurnState> lobbyTurnStates = new HashMap<>();

    public enum TurnState {
        WAITING_FOR_PLAYERS,        // Lobby created, waiting for players to join
        WAITING_FOR_START,          // At least 3 players, waiting for host to start
        PLAYERS_TURN_ROLL_DICE,     // Current player needs to roll dice
        PLAYERS_TURN_MOVE,          // Current player needs to move
        PLAYERS_TURN_SUSPECT,       // Current player can make a suggestion (optional, only if in room)
        PLAYERS_TURN_SOLVE,         // Current player can make an accusation (optional, anytime during turn)
        PLAYERS_TURN_END,           // Current player's turn is ending
        PLAYER_HAS_WON              // Game finished, someone won
    }

    /**
     * Initialize lobby state when lobby is created
     */
    public void initializeLobbyState(String lobbyId) {
        lobbyTurnStates.put(lobbyId, TurnState.WAITING_FOR_PLAYERS);
        logger.info("Initialized lobby state for lobby: {}", lobbyId);
        notifyStateChange(lobbyId);
    }
    
    /**
     * Update state when enough players join (3+)
     */
    public void setWaitingForStart(String lobbyId) {
        if (getTurnState(lobbyId) == TurnState.WAITING_FOR_PLAYERS) {
            lobbyTurnStates.put(lobbyId, TurnState.WAITING_FOR_START);
            logger.info("Lobby {} now waiting for start", lobbyId);
            notifyStateChange(lobbyId);
        }
    }
    
    /**
     * Update state when players drop below 3
     */
    public void setWaitingForPlayers(String lobbyId) {
        if (getTurnState(lobbyId) == TurnState.WAITING_FOR_START) {
            lobbyTurnStates.put(lobbyId, TurnState.WAITING_FOR_PLAYERS);
            logger.info("Lobby {} now waiting for more players", lobbyId);
            notifyStateChange(lobbyId);
        }
    }
    
    /**
     * Initialize turn state when game starts
     */
    public void initializeTurnState(String lobbyId) {
        lobbyTurnStates.put(lobbyId, TurnState.PLAYERS_TURN_ROLL_DICE);
        logger.info("Game started in lobby: {}", lobbyId);
        notifyCurrentTurn(lobbyId);
    }

    /**
     * Check if it's a specific player's turn
     */
    public boolean isPlayerTurn(String lobbyId, String playerName) {
        GameManager game = gameService.getGame(lobbyId);
        if (game == null) {
            return false;
        }

        Player currentPlayer = game.getCurrentPlayer();
        return currentPlayer != null && currentPlayer.getName().equals(playerName);
    }

    /**
     * Get current turn state for a lobby
     */
    public TurnState getTurnState(String lobbyId) {
        return lobbyTurnStates.getOrDefault(lobbyId, TurnState.WAITING_FOR_PLAYERS);
    }

    /**
     * Process dice roll and advance to movement phase
     */
    public boolean processDiceRoll(String lobbyId, String playerName, int diceValue) {
        TurnState currentState = getTurnState(lobbyId);
        if (currentState != TurnState.PLAYERS_TURN_ROLL_DICE) {
            logger.warn("Invalid turn state for dice roll in lobby {}: {}", lobbyId, currentState);
            return false;
        }

        GameManager game = gameService.getGame(lobbyId);
        game.setDiceRollS(diceValue);

        // Advance to movement phase
        lobbyTurnStates.put(lobbyId, TurnState.PLAYERS_TURN_MOVE);

        // Notify all players about dice roll and state change
        messagingTemplate.convertAndSend("/topic/diceRolled/" + lobbyId,
                Map.of("player", playerName, "diceValue", diceValue, "turnState", TurnState.PLAYERS_TURN_MOVE));

        logger.info("Player {} rolled {} in lobby {}", playerName, diceValue, lobbyId);
        return true;
    }

    /**
     * Process player movement and determine next phase
     */
    public boolean processMovement(String lobbyId, String playerName) {
        TurnState currentState = getTurnState(lobbyId);
        if (currentState != TurnState.PLAYERS_TURN_MOVE) {
            logger.warn("Invalid turn state for movement in lobby {}: {}", lobbyId, currentState);
            return false;
        }

        GameManager game = gameService.getGame(lobbyId);
        Player currentPlayer = game.getCurrentPlayer();

        // Check if player is in a room after movement
        if (game.inRoom(currentPlayer)) {
            // Player can make a suggestion
            lobbyTurnStates.put(lobbyId, TurnState.PLAYERS_TURN_SUSPECT);
            messagingTemplate.convertAndSend("/topic/turnStateChanged/" + lobbyId,
                    Map.of("turnState", TurnState.PLAYERS_TURN_SUSPECT, "currentPlayer", currentPlayer.getName()));
        } else {
            // End turn if not in room
            endTurn(lobbyId);
        }

        return true;
    }

    /**
     * Process suggestion and end turn
     */
    public boolean processSuggestion(String lobbyId, String playerName, String suspect, String weapon) {
        TurnState currentState = getTurnState(lobbyId);
        if (currentState != TurnState.PLAYERS_TURN_SUSPECT) {
            logger.warn("Invalid turn state for suggestion in lobby {}: {}", lobbyId, currentState);
            return false;
        }

        GameManager game = gameService.getGame(lobbyId);
        Player currentPlayer = game.getCurrentPlayer();

        // Make the suggestion
        boolean suggestionDisproved = game.makeSuggestion(currentPlayer, suspect, weapon);

        // Notify all players about the suggestion
        messagingTemplate.convertAndSend("/topic/suggestionMade/" + lobbyId,
                Map.of("player", playerName, "suspect", suspect, "weapon", weapon, "disproved", suggestionDisproved));

        // End turn after suggestion
        endTurn(lobbyId);

        logger.info("Player {} made suggestion in lobby {}: {} with {}", playerName, lobbyId, suspect, weapon);
        return true;
    }

    /**
     * Process accusation and potentially end game
     */
    public boolean processAccusation(String lobbyId, String playerName, String suspect, String weapon, String room) {
        if (!isPlayerTurn(lobbyId, playerName)) {
            return false;
        }

        // Can make accusation during any turn phase except PLAYERS_TURN_END
        TurnState currentState = getTurnState(lobbyId);
        if (currentState == TurnState.PLAYERS_TURN_END || currentState == TurnState.PLAYER_HAS_WON) {
            return false;
        }

        GameManager game = gameService.getGame(lobbyId);
        Player currentPlayer = game.getCurrentPlayer();

        // Check if accusation is correct
        boolean isCorrect = game.getCorrectSuspect().equals(suspect) &&
                game.getCorrectWeapon().equals(weapon) &&
                game.getCorrectRoom().equals(room);

        if (isCorrect) {
            // Player wins
            currentPlayer.setHasWon(true);
            game.setState(GameState.ENDED);
            lobbyTurnStates.put(lobbyId, TurnState.PLAYER_HAS_WON);

            messagingTemplate.convertAndSend("/topic/gameWon/" + lobbyId,
                    Map.of("winner", playerName, "suspect", suspect, "weapon", weapon, "room", room));

            logger.info("Player {} won the game in lobby {} with correct accusation", playerName, lobbyId);
        } else {
            // Player is eliminated
            currentPlayer.setActive(false);

            messagingTemplate.convertAndSend("/topic/playerEliminated/" + lobbyId,
                    Map.of("player", playerName, "suspect", suspect, "weapon", weapon, "room", room));

            // Check if game should end (only one player left)
            if (game.checkGameEnd()) {
                lobbyTurnStates.put(lobbyId, TurnState.PLAYER_HAS_WON);
                messagingTemplate.convertAndSend("/topic/gameEnded/" + lobbyId,
                        Map.of("reason", "Only one player remaining"));
            } else {
                // Continue to next player
                nextTurn(lobbyId);
            }

            logger.info("Player {} was eliminated in lobby {} with wrong accusation", playerName, lobbyId);
        }

        return true;
    }

    /**
     * End current turn and move to next player
     */
    public void endTurn(String lobbyId) {
        GameManager game = gameService.getGame(lobbyId);
        if (game == null || game.checkGameEnd()) {
            lobbyTurnStates.put(lobbyId, TurnState.PLAYER_HAS_WON);
            return;
        }

        lobbyTurnStates.put(lobbyId, TurnState.PLAYERS_TURN_END);

        nextTurn(lobbyId);
    }

    /**
     * Move to next player's turn
     */
    public void nextTurn(String lobbyId) {
        GameManager game = gameService.getGame(lobbyId);
        if (game == null || game.checkGameEnd()) {
            lobbyTurnStates.put(lobbyId, TurnState.PLAYER_HAS_WON);
            return;
        }

        game.nextTurn();
        lobbyTurnStates.put(lobbyId, TurnState.PLAYERS_TURN_ROLL_DICE);

        notifyCurrentTurn(lobbyId);

        logger.info("Advanced to next turn in lobby {}", lobbyId);
    }

    /**
     * Skip current player's turn (for eliminated players or timeouts)
     */
    public void skipTurn(String lobbyId, String reason) {
        logger.info("Skipping turn in lobby {} - reason: {}", lobbyId, reason);
        messagingTemplate.convertAndSend("/topic/turnSkipped/" + lobbyId,
                Map.of("reason", reason));
        nextTurn(lobbyId);
    }

    /**
     * Skip suggestion phase and end turn
     */
    public void skipSuggestion(String lobbyId, String playerName) {
        if (!isPlayerTurn(lobbyId, playerName)) {
            return;
        }

        TurnState currentState = getTurnState(lobbyId);
        if (currentState == TurnState.PLAYERS_TURN_SUSPECT) {
            endTurn(lobbyId);
            logger.info("Player {} skipped suggestion in lobby {}", playerName, lobbyId);
        }
    }

    /**
     * Get current player for a lobby
     */
    public Player getCurrentPlayer(String lobbyId) {
        GameManager game = gameService.getGame(lobbyId);
        return game != null ? game.getCurrentPlayer() : null;
    }

    /**
     * Check if player can make an accusation
     */
    public boolean canMakeAccusation(String lobbyId, String playerName) {
        if (!isPlayerTurn(lobbyId, playerName)) {
            return false;
        }

        TurnState state = getTurnState(lobbyId);
        return state == TurnState.PLAYERS_TURN_ROLL_DICE || 
               state == TurnState.PLAYERS_TURN_MOVE || 
               state == TurnState.PLAYERS_TURN_SUSPECT;
    }

    /**
     * Check if player can make a suggestion
     */
    public boolean canMakeSuggestion(String lobbyId, String playerName) {
        if (!isPlayerTurn(lobbyId, playerName)) {
            return false;
        }

        return getTurnState(lobbyId) == TurnState.PLAYERS_TURN_SUSPECT;
    }

    /**
     * Force end game (for admin purposes)
     */
    public void forceEndGame(String lobbyId) {
        lobbyTurnStates.put(lobbyId, TurnState.PLAYER_HAS_WON);
        messagingTemplate.convertAndSend("/topic/gameEnded/" + lobbyId,
                Map.of("reason", "Game ended by admin"));
        logger.info("Game forcefully ended in lobby {}", lobbyId);
    }

    /**
     * Notify all players about current turn
     */
    private void notifyCurrentTurn(String lobbyId) {
        GameManager game = gameService.getGame(lobbyId);
        if (game == null) {
            return;
        }

        Player currentPlayer = game.getCurrentPlayer();
        TurnState turnState = getTurnState(lobbyId);

        messagingTemplate.convertAndSend("/topic/currentTurn/" + lobbyId,
                Map.of("currentPlayer", currentPlayer.getName(),
                        "turnState", turnState,
                        "playerIndex", game.getCurrentPlayerIndex()));
    }

    /**
     * Notify about state changes
     */
    private void notifyStateChange(String lobbyId) {
        TurnState turnState = getTurnState(lobbyId);
        
        messagingTemplate.convertAndSend("/topic/stateChanged/" + lobbyId, 
            Map.of("turnState", turnState, "lobbyId", lobbyId));
    }
}
