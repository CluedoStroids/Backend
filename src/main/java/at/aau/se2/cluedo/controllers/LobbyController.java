package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.models.Lobby;
import at.aau.se2.cluedo.services.LobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class LobbyController {

    @Autowired
    private LobbyService lobbyService;

    static class CreateLobbyRequest {
        private String username;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    static class JoinLobbyRequest {
        private String username;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    @MessageMapping("/createLobby")
    @SendTo("/topic/lobbyCreated")
    public String createLobby(CreateLobbyRequest request) {
        return lobbyService.createLobby(request.getUsername());
    }

    @MessageMapping("/joinLobby/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public Lobby joinLobby(@DestinationVariable String lobbyId, JoinLobbyRequest request) {
        lobbyService.joinLobby(lobbyId, request.getUsername());
        return lobbyService.getLobby(lobbyId);
    }
}