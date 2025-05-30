package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.GameStartedResponse;
import at.aau.se2.cluedo.dto.StartGameRequest;
import at.aau.se2.cluedo.dto.TurnStateResponse;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.lobby.Lobby;
import at.aau.se2.cluedo.services.GameService;
import at.aau.se2.cluedo.services.LobbyService;

import java.util.List;
import java.util.Map;

import at.aau.se2.cluedo.services.TurnService;
import at.aau.se2.cluedo.services.TurnService.TurnState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private GameService gameService;

    @Autowired
    private LobbyService lobbyService;
    @Autowired
    private TurnService turnService;

    @MessageMapping("/startGame/{lobbyId}")
    @SendTo("/topic/gameStarted/{lobbyId}")
    public GameStartedResponse startGame(@DestinationVariable String lobbyId, StartGameRequest request) {
        Player initiator = request.getPlayer();


        Lobby lobby = lobbyService.getLobby(lobbyId);

        Player lobbyPlayer = lobby.getPlayers().stream()
                .filter(p -> p.getName().equals(initiator.getName()))
                .findFirst()
                .orElse(null);

        if (lobbyPlayer == null) {

            throw new IllegalStateException("Player not found in lobby");
        }

        if (!lobby.getHostId().equals(lobbyPlayer.getPlayerID())) {

            throw new IllegalStateException("Only the host can start the game");
        }

        if (!gameService.canStartGame(lobbyId)) {

            throw new IllegalStateException("Not enough players to start a game. Minimum required: 3");
        }

        try {
            GameManager gameManager = gameService.startGameFromLobby(lobbyId);

            List<Player> gamePlayers = gameManager.getPlayers();

            turnService.initializeTurnState(lobbyId);
            logger.info("Game started and turn system initialized for lobby: {}", lobbyId);

            return (new GameStartedResponse(lobbyId, gamePlayers));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start game: " + e.getMessage());
        }
    }

    @MessageMapping("/initializeTurns/{lobbyId}")
    @SendTo("/topic/turnsInitialized/{lobbyId}")
    public TurnStateResponse initializeTurns(@DestinationVariable String lobbyId) {
        try {
            turnService.initializeTurnState(lobbyId);

            GameManager game = gameService.getGame(lobbyId);
            Player currentPlayer = game.getCurrentPlayer();

            return new TurnStateResponse(
                lobbyId,
                currentPlayer.getName(),
                game.getCurrentPlayerIndex(),
                TurnState.PLAYERS_TURN_ROLL_DICE,
                false, // Cannot make suggestion until in room
                true,  // Can make accusation during turn
                0,
                "Game started! " + currentPlayer.getName() + "'s turn to roll dice."
            );
        } catch (Exception e) {
            logger.error("Failed to initialize turns for lobby {}: {}", lobbyId, e.getMessage());
            throw new IllegalStateException("Failed to initialize turns: " + e.getMessage());
        }
    }

    @MessageMapping("/getTurnState/{lobbyId}")
    @SendTo("/topic/currentTurnState/{lobbyId}")
    public TurnStateResponse getTurnState(@DestinationVariable String lobbyId) {
        try {
            GameManager game = gameService.getGame(lobbyId);
            if (game == null) {
                return createErrorResponse(lobbyId, "Game not found");
            }

            Player currentPlayer = game.getCurrentPlayer();
            TurnState currentState = turnService.getTurnState(lobbyId);

            boolean canMakeSuggestion = currentState == TurnState.PLAYERS_TURN_SUGGEST;
            boolean canMakeAccusation = turnService.canMakeAccusation(lobbyId, currentPlayer.getName());

            String message = generateTurnMessage(currentPlayer.getName(), currentState);

            return new TurnStateResponse(
                lobbyId,
                currentPlayer.getName(),
                game.getCurrentPlayerIndex(),
                currentState,
                canMakeSuggestion,
                canMakeAccusation,
                game.getDiceRollS(),
                message
            );
        } catch (Exception e) {
            logger.error("Error getting turn state for lobby {}: {}", lobbyId, e.getMessage());
            return createErrorResponse(lobbyId, "Error getting turn state");
        }
    }

    private TurnStateResponse createErrorResponse(String lobbyId, String message) {
        return new TurnStateResponse(
            lobbyId,
            "",
            -1,
            TurnState.PLAYER_HAS_WON,
            false,
            false,
            0,
            message
        );
    }

    private String generateTurnMessage(String playerName, TurnState state) {
        return switch (state) {
            case WAITING_FOR_PLAYERS -> "Waiting for more players to join...";
            case WAITING_FOR_START -> "Waiting for host to start the game...";
            case PLAYERS_TURN_ROLL_DICE -> playerName + "'s turn - Roll the dice to start your turn!";
            case PLAYERS_TURN_MOVE -> playerName + "'s turn - Move your piece on the board!";
            case PLAYERS_TURN_SUGGEST -> playerName + " is in a room - Make a suggestion or accusation!";
            case PLAYERS_TURN_SOLVE -> playerName + " can make an accusation!";
            case PLAYERS_TURN_END -> "Turn ending, moving to next player...";
            case PLAYER_HAS_WON -> "Game has ended!";
        };
    }
}
