package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gamemanager.GameState;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
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
    // turn states for each lobby
    private final Map<String, TurnState> lobbyTurnStates = new HashMap<>();
    @Autowired
    private GameService gameService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Initialize lobby state when created
     *
     * @param lobbyId
     */
    public void initializeLobbyState(String lobbyId) {
        lobbyTurnStates.put(lobbyId, TurnState.WAITING_FOR_PLAYERS);
        logger.info("Initialized lobby state for lobby: {}", lobbyId);
        notifyStateChange(lobbyId);
    }

    /**
     * Update state when 3 players join
     *
     * @param lobbyId
     */
    public void setWaitingForStart(String lobbyId) {
        if (getTurnState(lobbyId) == TurnState.WAITING_FOR_PLAYERS) {
            lobbyTurnStates.put(lobbyId, TurnState.WAITING_FOR_START);
            logger.info("Lobby {} now waiting for start", lobbyId);
            notifyStateChange(lobbyId);
        }
    }

    /**
     * Update state when players leave
     *
     * @param lobbyId
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
     *
     * @param lobbyId
     */
    public void initializeTurnState(String lobbyId) {
        lobbyTurnStates.put(lobbyId, TurnState.PLAYERS_TURN_ROLL_DICE);
        logger.info("Game started in lobby: {}", lobbyId);
        notifyCurrentTurn(lobbyId);
    }

    /**
     * Check if it's the player's turn
     *
     * @param lobbyId
     * @param playerName
     * @return true if it is the player's turn
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
     *
     * @param lobbyId
     * @return lobbystate for lobby
     */
    public TurnState getTurnState(String lobbyId) {
        return lobbyTurnStates.getOrDefault(lobbyId, TurnState.WAITING_FOR_PLAYERS);
    }

    /**
     * Process dice roll and determine next phase
     *
     * @param lobbyId
     * @param playerName
     * @param diceValue
     * @return true if successful
     */
    public boolean processDiceRoll(String lobbyId, String playerName, int diceValue) {
        TurnState currentState = getTurnState(lobbyId);
        if (currentState != TurnState.PLAYERS_TURN_ROLL_DICE) {
            logger.warn("Invalid turn state for dice roll in lobby {}: {}", lobbyId, currentState);
            return false;
        }

        GameManager game = gameService.getGame(lobbyId);
        game.setDiceRollS(diceValue);

        // advance to next (movement) phase
        lobbyTurnStates.put(lobbyId, TurnState.PLAYERS_TURN_MOVE);

        // notify all players about dice roll and state change
        messagingTemplate.convertAndSend("/topic/diceRolled/" + lobbyId,
                Map.of("player", playerName, "diceValue", diceValue, "turnState", TurnState.PLAYERS_TURN_MOVE));

        logger.info("Player {} rolled {} in lobby {}", playerName, diceValue, lobbyId);
        return true;
    }

    /**
     * Process movement and determine next phase
     *
     * @param lobbyId
     * @param playerName
     * @return true if successful
     */
    public boolean processMovement(String lobbyId, String playerName) {
        GameManager game = gameService.getGame(lobbyId);
        Player currentPlayer = game.getCurrentPlayer();

        if (playerName.equals(currentPlayer.getName())) {
            // check if player is in a room after movement
            if (game.inRoom(currentPlayer)) {
                // player can make a suspicion
                lobbyTurnStates.put(lobbyId, TurnState.PLAYERS_TURN_SUGGEST);
                messagingTemplate.convertAndSend("/topic/turnStateChanged/" + lobbyId,
                        Map.of("turnState", TurnState.PLAYERS_TURN_SUGGEST, "currentPlayer", currentPlayer.getName()));
                logger.info("Player {} is in a room in lobby {}, turn state changed to suggestion", playerName, lobbyId);
            } else {
                // end turn if not in room
                endTurn(lobbyId);
            }
            return true;
        }
        return false;
    }

    /**
     * Process suggestion and handle turn logic, AKA suspicion
     * @param lobbyId
     * @param playerName
     * @param suspect
     * @param weapon
     * @return true if successful
     */
    public boolean processSuggestion(String lobbyId, String playerName, String suspect, String weapon) {
        if (!isPlayerTurn(lobbyId, playerName)) {
            return false;
        }

        TurnState currentState = getTurnState(lobbyId);
        if (currentState != TurnState.PLAYERS_TURN_SUGGEST) {
            logger.warn("Invalid turn state for suggestion in lobby {}: {}", lobbyId, currentState);
            return false;
        }

        GameManager game = gameService.getGame(lobbyId);
        Player currentPlayer = game.getCurrentPlayer();

        // make the suggestion using GameManager's makeSuggestion method
        boolean suggestionDisproved = game.makeSuggestion(currentPlayer, suspect, weapon);

        // get room name from player's current position
        String room = game.getGameBoard().getCell(currentPlayer.getX(), currentPlayer.getY()).getRoom().getName();

        // notify all players about the suggestion
        messagingTemplate.convertAndSend("/topic/suggestionMade/" + lobbyId,
                Map.of("player", playerName, "suspect", suspect, "weapon", weapon, "room", room,
                        "disproved", suggestionDisproved, "success", true));

        // end turn after suggestion
        endTurn(lobbyId);

        logger.info("Player {} made suggestion in lobby {}: {} with {}", playerName, lobbyId, suspect, weapon);
        return true;
    }

    /**
     * Process accusation and handle game logic AKA Solve case file
     * @param lobbyId
     * @param playerName
     * @param suspect
     * @param weapon
     * @param room
     * @return true if successful
     */
    public boolean processAccusation(String lobbyId, String playerName, String suspect, String weapon, String room) {
        logger.info("Player {} made accusation in lobby {}: {} with {} in {}", playerName, lobbyId, suspect, weapon, room);
        if (!isPlayerTurn(lobbyId, playerName)) {
            return false;
        }

        TurnState currentState = getTurnState(lobbyId);
        if (currentState == TurnState.PLAYERS_TURN_END || currentState == TurnState.PLAYER_HAS_WON) {
            logger.warn("Invalid turn state for accusation in lobby {}: {}", lobbyId, currentState);
            return false;
        }

        GameManager game = gameService.getGame(lobbyId);
        Player currentPlayer = game.getCurrentPlayer();
        SecretFile secretFile = new SecretFile(game.getCardByName(room), game.getCardByName(weapon), game.getCardByName(suspect));

        // check if accusation is correct using direct comparison
        boolean isCorrect = game.makeAccusation(currentPlayer, secretFile);

        if (isCorrect) {
            // player wins
            currentPlayer.setHasWon(true);
            game.setState(GameState.ENDED);
            lobbyTurnStates.put(lobbyId, TurnState.PLAYER_HAS_WON);

            // notify all players about the win
            messagingTemplate.convertAndSend("/topic/accusationMade/" + lobbyId,
                    Map.of("player", playerName, "suspect", suspect, "weapon", weapon, "room", room,
                            "correct", true, "gameWon", true, "success", true));

            logger.info("Player {} won the game in lobby {} with correct accusation!", playerName, lobbyId);
        } else {
            // eliminate player
            currentPlayer.setActive(false);
            nextTurn(lobbyId);

            // notify all players about the failed accusation
            messagingTemplate.convertAndSend("/topic/accusationMade/" + lobbyId,
                    Map.of("player", playerName, "suspect", suspect, "weapon", weapon, "room", room,
                            "correct", false, "playerEliminated", true, "success", true));

            // check if game should end (only one player left)
            if (game.checkGameEnd()) {
                lobbyTurnStates.put(lobbyId, TurnState.PLAYER_HAS_WON);
            } else {
                // end turn and move to next player
                endTurn(lobbyId);
            }

            logger.info("Player {} eliminated in lobby {} with incorrect accusation", playerName, lobbyId);
        }

        return true;
    }

    /**
     * End current player's turn
     * @param lobbyId
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
     * Advance to next player's turn
     * @param lobbyId
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
     * Skip current player's turn
     * @param lobbyId
     * @param reason
     */
    public void skipTurn(String lobbyId, String reason) {
        logger.info("Skipping turn in lobby {} - reason: {}", lobbyId, reason);
        messagingTemplate.convertAndSend("/topic/turnSkipped/" + lobbyId,
                Map.of("reason", reason));
        nextTurn(lobbyId);
    }

    /**
     * Get current player for a lobby
     * @param lobbyId
     * @return current player
     */
    public Player getCurrentPlayer(String lobbyId) {
        GameManager game = gameService.getGame(lobbyId);
        return game != null ? game.getCurrentPlayer() : null;
    }

    /**
     * Check if player can make a suggestion
     * @param lobbyId
     * @param playerName
     * @return true if player can make a suggestion
     */
    public boolean canMakeSuggestion(String lobbyId, String playerName) {
        if (!isPlayerTurn(lobbyId, playerName)) {
            return false;
        }

        return getTurnState(lobbyId) == TurnState.PLAYERS_TURN_SUGGEST;
    }

    /**
     * Check if player can make an accusation
     * @param lobbyId
     * @param playerName
     * @return true if player can make an accusation
     */
    public boolean canMakeAccusation(String lobbyId, String playerName) {
        if (!isPlayerTurn(lobbyId, playerName)) {
            return false;
        }

        TurnState state = getTurnState(lobbyId);
        return state == TurnState.PLAYERS_TURN_ROLL_DICE ||
                state == TurnState.PLAYERS_TURN_MOVE ||
                state == TurnState.PLAYERS_TURN_SUGGEST;
    }

    /**
     * Notify about current turn
     * @param lobbyId
     */
    private void notifyCurrentTurn(String lobbyId) {
        GameManager game = gameService.getGame(lobbyId);

        Player currentPlayer = game.getCurrentPlayer();
        TurnState turnState = getTurnState(lobbyId);

        messagingTemplate.convertAndSend("/topic/currentTurn/" + lobbyId,
                Map.of("currentPlayer", currentPlayer.getName(),
                        "turnState", turnState,
                        "playerIndex", game.getCurrentPlayerIndex()));
    }

    /**
     * Notify about state change
     * @param lobbyId
     */
    private void notifyStateChange(String lobbyId) {
        TurnState turnState = getTurnState(lobbyId);

        messagingTemplate.convertAndSend("/topic/stateChanged/" + lobbyId,
                Map.of("turnState", turnState, "lobbyId", lobbyId));
    }

    public enum TurnState {
        WAITING_FOR_PLAYERS,        // when lobby created, waiting for players to join
        WAITING_FOR_START,          // when at least 3 players, waiting for host to start
        PLAYERS_TURN_ROLL_DICE,     // player needs to roll dice
        PLAYERS_TURN_MOVE,          // player needs to move
        PLAYERS_TURN_SUGGEST,       // player can make a suggestion (if in room)
        PLAYERS_TURN_SOLVE,         // player can make a solve case attempt (optional, anytime during turn)
        PLAYERS_TURN_END,           // player's turn is ending
        PLAYER_HAS_WON              // game finished, someone won
    }
}
