package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.CreateLobbyRequest;
import at.aau.se2.cluedo.dto.JoinLobbyRequest;
import at.aau.se2.cluedo.dto.LeaveLobbyRequest;
import at.aau.se2.cluedo.dto.LobbyResponse;
import at.aau.se2.cluedo.models.lobby.Lobby;
import at.aau.se2.cluedo.services.LobbyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import at.aau.se2.cluedo.dto.SolveCaseRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;

@Controller
public class LobbyController {

    private static final Logger logger = LoggerFactory.getLogger(LobbyController.class);

    @Autowired
    private LobbyService lobbyService;

    @MessageMapping("/createLobby")
    @SendTo("/topic/lobbyCreated")
    public String createLobby(CreateLobbyRequest request) {
        logger.info("Creating lobby for user: {}", request.getUsername());
        return lobbyService.createLobby(request.getUsername());
    }

    @MessageMapping("/joinLobby/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public LobbyResponse joinLobby(@DestinationVariable String lobbyId, JoinLobbyRequest request) {
        logger.info("User {} joining lobby: {}", request.getUsername(), lobbyId);
        lobbyService.joinLobby(lobbyId, request.getUsername());
        Lobby lobby = lobbyService.getLobby(lobbyId);
        return LobbyResponse.fromLobby(lobby);
    }

    @MessageMapping("/leaveLobby/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public LobbyResponse leaveLobby(@DestinationVariable String lobbyId, LeaveLobbyRequest request) {
        logger.info("User {} leaving lobby: {}", request.getUsername(), lobbyId);
        lobbyService.leaveLobby(lobbyId, request.getUsername());
        Lobby lobby = lobbyService.getLobby(lobbyId);
        return LobbyResponse.fromLobby(lobby);
    }
    @MessageMapping("/solveCase")
    public void solveCase(SolveCaseRequest request) {
        lobbyService.solveCase(request);
    }

}