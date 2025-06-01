package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.*;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.lobby.Lobby;
import at.aau.se2.cluedo.services.GameService;
import at.aau.se2.cluedo.services.LobbyService;
import at.aau.se2.cluedo.services.TurnService;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class LobbyController {

    private static final Logger logger = LoggerFactory.getLogger(LobbyController.class);

    private static LobbyController instance = null;

    public static LobbyController getInstance(){
        if(instance == null){
            instance = new LobbyController();
        }
        return instance;
    }

    public LobbyService getLobbyService() {
        return lobbyService;
    }

    public GameService getGameService() {
        return gameService;
    }

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private GameService gameService;

    @Autowired
    private TurnService turnService;

    @MessageMapping("/createLobby")
    @SendTo("/topic/lobbyCreated")
    public String createLobby(CreateLobbyRequest request) {
        Player player = request.getPlayer();

        lobbyService.setGameService(gameService);
        String lobbyId = lobbyService.createLobby(player);

        // Initialize lobby state
        turnService.initializeLobbyState(lobbyId);

        return lobbyId;
    }

    @MessageMapping("/joinLobby/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public LobbyResponse joinLobby(@DestinationVariable String lobbyId, JoinLobbyRequest request) {
        Player player = request.getPlayer();

        lobbyService.joinLobby(lobbyId, player);
        Lobby lobby = lobbyService.getLobby(lobbyId);
        lobbyService.setGameService(gameService);

        // Check if we now have enough players to start
        if (lobby.getPlayers().size() >= 3) {
            turnService.setWaitingForStart(lobbyId);
        }

        return LobbyResponse.fromLobby(lobby);
    }

    @MessageMapping("/leaveLobby/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public LobbyResponse leaveLobby(@DestinationVariable String lobbyId, LeaveLobbyRequest request) {
        Player player = request.getPlayer();

        lobbyService.leaveLobby(lobbyId, player);
        Lobby lobby = lobbyService.getLobby(lobbyId);

        // Check if we no longer have enough players to start
        if (lobby.getPlayers().size() < 3) {
            turnService.setWaitingForPlayers(lobbyId);
        }

        return LobbyResponse.fromLobby(lobby);
    }

    @MessageMapping("/getActiveLobbies")
    @SendTo("/topic/activeLobbies")
    public ActiveLobbiesResponse getActiveLobbies(GetActiveLobbiesRequest request) {

        List<Lobby> activeLobbies = lobbyService.getAllActiveLobbies();
        return ActiveLobbiesResponse.fromLobbies(activeLobbies);
    }

    @MessageMapping("/canStartGame/{lobbyId}")
    @SendTo("/topic/canStartGame/{lobbyId}")
    public CanStartGameResponse canStartGame(@DestinationVariable String lobbyId) {

        boolean canStart = gameService.canStartGame(lobbyId);
        return new CanStartGameResponse(canStart);
    }

    @MessageMapping("/solveCase")
    public void solveCase(SolveCaseRequest request) {
        gameService.processSolveCase(request);
    }

    @MessageMapping("/skipTurn/{lobbyId}")
    @SendTo("/topic/turnSkipped/{lobbyId}")
    public TurnStateResponse skipTurn(@DestinationVariable String lobbyId, Map<String, String> request) {
        try {
            String reason = request.getOrDefault("reason", "Turn skipped");
            turnService.skipTurn(lobbyId, reason);

            GameManager game = gameService.getGame(lobbyId);
            Player currentPlayer = game.getCurrentPlayer();

            return new TurnStateResponse(
                lobbyId,
                currentPlayer.getName(),
                game.getCurrentPlayerIndex(),
                TurnService.TurnState.PLAYERS_TURN_ROLL_DICE,
                false, // Cannot make suggestion at start of turn
                true,  // Can make accusation during turn
                0,
                "Turn skipped. " + currentPlayer.getName() + "'s turn to roll dice."
            );
        } catch (Exception e) {
            logger.error("Error skipping turn in lobby {}: {}", lobbyId, e.getMessage());
            return createErrorResponse(lobbyId, "Error skipping turn");
        }
    }

    @MessageMapping("/checkPlayerTurn/{lobbyId}")
    @SendTo("/topic/playerTurnCheck/{lobbyId}")
    public Map<String, Object> checkPlayerTurn(@DestinationVariable String lobbyId, Map<String, String> request) {
        try {
            String playerName = request.get("playerName");
            boolean isPlayerTurn = turnService.isPlayerTurn(lobbyId, playerName);
            TurnService.TurnState currentState = turnService.getTurnState(lobbyId);

            return Map.of(
                "lobbyId", lobbyId,
                "playerName", playerName,
                "isPlayerTurn", isPlayerTurn,
                "turnState", currentState,
                "canMakeSuggestion", currentState == TurnService.TurnState.PLAYERS_TURN_SUGGEST,
                "canMakeAccusation", turnService.canMakeAccusation(lobbyId, playerName)
            );
        } catch (Exception e) {
            logger.error("Error checking player turn in lobby {}: {}", lobbyId, e.getMessage());
            return Map.of(
                "lobbyId", lobbyId,
                "error", "Error checking player turn"
            );
        }
    }

    private TurnStateResponse createErrorResponse(String lobbyId, String message) {
        return new TurnStateResponse(
            lobbyId,
            "",
            -1,
            TurnService.TurnState.PLAYER_HAS_WON,
            false,
            false,
            0,
            message
        );
    }
}