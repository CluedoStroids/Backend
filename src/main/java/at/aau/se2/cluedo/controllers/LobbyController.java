package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.*;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.lobby.Lobby;
import at.aau.se2.cluedo.services.GameService;
import at.aau.se2.cluedo.services.LobbyService;
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

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private GameService gameService;

    @MessageMapping("/createLobby")
    @SendTo("/topic/lobbyCreated")
    public String createLobby(CreateLobbyRequest request) {
        Player player = request.getPlayer();
        logger.info("Creating lobby for player: {}", player.getName());
        return lobbyService.createLobby(player);
    }

    @MessageMapping("/joinLobby/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public LobbyResponse joinLobby(@DestinationVariable String lobbyId, JoinLobbyRequest request) {
        Player player = request.getPlayer();
        logger.info("Player {} joining lobby: {}", player.getName(), lobbyId);
        lobbyService.joinLobby(lobbyId, player);
        Lobby lobby = lobbyService.getLobby(lobbyId);
        return LobbyResponse.fromLobby(lobby);
    }

    @MessageMapping("/leaveLobby/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public LobbyResponse leaveLobby(@DestinationVariable String lobbyId, LeaveLobbyRequest request) {
        Player player = request.getPlayer();
        logger.info("Player {} leaving lobby: {}", player.getName(), lobbyId);
        lobbyService.leaveLobby(lobbyId, player);
        Lobby lobby = lobbyService.getLobby(lobbyId);
        return LobbyResponse.fromLobby(lobby);
    }

    @MessageMapping("/getActiveLobbies")
    @SendTo("/topic/activeLobbies")
    public ActiveLobbiesResponse getActiveLobbies(GetActiveLobbiesRequest request) {
        logger.info("Getting all active lobbies");
        List<Lobby> activeLobbies = lobbyService.getAllActiveLobbies();
        return ActiveLobbiesResponse.fromLobbies(activeLobbies);
    }

    @MessageMapping("/canStartGame/{lobbyId}")
    @SendTo("/topic/canStartGame/{lobbyId}")
    public CanStartGameResponse canStartGame(@DestinationVariable String lobbyId) {
        logger.info("Checking if lobby {} can start a game", lobbyId);
        boolean canStart = gameService.canStartGame(lobbyId);
        return new CanStartGameResponse(canStart);
    }

    @MessageMapping("/solveCase")
    public void solveCase(SolveCaseRequest request) {
        logger.info("Received solve case attempt from {}", request.getUsername());
        gameService.processSolveCase(request);
    }

}