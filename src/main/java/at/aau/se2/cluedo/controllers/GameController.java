package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.GameStartedResponse;
import at.aau.se2.cluedo.dto.StartGameRequest;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.lobby.Lobby;
import at.aau.se2.cluedo.services.GameService;
import at.aau.se2.cluedo.services.LobbyService;

import java.util.List;
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

    @MessageMapping("/startGame/{lobbyId}")
    @SendTo("/topic/gameStarted/{lobbyId}")
    public GameStartedResponse startGame(@DestinationVariable String lobbyId, StartGameRequest request) {
        Player initiator = request.getPlayer();
        logger.info("Player {} is attempting to start a game from lobby {}", initiator.getName(), lobbyId);

        Lobby lobby = lobbyService.getLobby(lobbyId);

        Player lobbyPlayer = lobby.getPlayers().stream()
                .filter(p -> p.getName().equals(initiator.getName()))
                .findFirst()
                .orElse(null);

        if (lobbyPlayer == null) {
            logger.warn("Player {} attempted to start a game but is not in lobby {}",
                    initiator.getName(), lobbyId);
            throw new IllegalStateException("Player not found in lobby");
        }

        if (!lobby.getHostId().equals(lobbyPlayer.getPlayerID())) {
            logger.warn("Player {} attempted to start a game but is not the host of lobby {}",
                    initiator.getName(), lobbyId);
            throw new IllegalStateException("Only the host can start the game");
        }

        if (!gameService.canStartGame(lobbyId)) {
            logger.warn("Not enough players in lobby {} to start a game", lobbyId);
            throw new IllegalStateException("Not enough players to start a game. Minimum required: 3");
        }

        try {
            GameManager gameManager = gameService.startGameFromLobby(lobbyId);
            logger.info("Game started successfully from lobby {}", lobbyId);

            List<Player> gamePlayers = gameManager.getPlayers();

            return new GameStartedResponse(lobbyId, gamePlayers);
        } catch (Exception e) {
            logger.error("Failed to start game from lobby {}: {}", lobbyId, e.getMessage());
            throw new IllegalStateException("Failed to start game: " + e.getMessage());
        }
    }
}
