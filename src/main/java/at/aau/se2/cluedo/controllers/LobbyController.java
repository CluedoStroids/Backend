package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.*;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.lobby.Lobby;
import at.aau.se2.cluedo.services.GameService;
import at.aau.se2.cluedo.services.LobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class LobbyController {


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

    @MessageMapping("/createLobby")
    @SendTo("/topic/lobbyCreated")
    public String createLobby(CreateLobbyRequest request) {
        Player player = request.getPlayer();

        return lobbyService.createLobby(player);
    }

    @MessageMapping("/joinLobby/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public LobbyResponse joinLobby(@DestinationVariable String lobbyId, JoinLobbyRequest request) {
        Player player = request.getPlayer();

        lobbyService.joinLobby(lobbyId, player);
        Lobby lobby = lobbyService.getLobby(lobbyId);
        return LobbyResponse.fromLobby(lobby);
    }

    @MessageMapping("/leaveLobby/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public LobbyResponse leaveLobby(@DestinationVariable String lobbyId, LeaveLobbyRequest request) {
        Player player = request.getPlayer();

        lobbyService.leaveLobby(lobbyId, player);
        Lobby lobby = lobbyService.getLobby(lobbyId);
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

}