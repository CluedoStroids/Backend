package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.GameStartedResponse;
import at.aau.se2.cluedo.dto.StartGameRequest;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
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
public class GameController {



    @Autowired
    private GameService gameService;

    @Autowired
    private LobbyService lobbyService;

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

            return (new GameStartedResponse(lobbyId, gamePlayers));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start game: " + e.getMessage());
        }
    }
}
