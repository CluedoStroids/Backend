package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.GameStartedResponse;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
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
public class GameplayController {

    private static final Logger logger = LoggerFactory.getLogger(GameplayController.class);

    @Autowired
    private GameService gameService;

    @MessageMapping("/makeSuggestion/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public void makeSuggestion(@DestinationVariable String lobbyId, Player player, String suspect, String weapon) {
        logger.info("User {} makes a suggestion.", player.getName());
        gameService.getGame(lobbyId).makeSuggestion(player, suspect, weapon);
    }

    @MessageMapping("/makeAccusation/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public void makeAccusation(@DestinationVariable String lobbyId, Player player, SecretFile acusation) {
        logger.info("User {} makes a accusation.", player.getName());
        gameService.getGame(lobbyId).makeAccusation(player, acusation);
    }

    @MessageMapping("/displayGameBoard/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public void displayGameBoard(@DestinationVariable String lobbyId,List<Player> players) {
        gameService.getGame(lobbyId).getGameBoard().displayGameBoard(players);
    }
    @MessageMapping("/performMovement/{lobbyId}")
    @SendTo("/topic/performedMovement/{lobbyId}")
    public GameStartedResponse performMovement(@DestinationVariable String lobbyId, Player player, List<String> movement) {
        logger.info("Player {} is attempting to start a movement in lobby {}", player, lobbyId);
        gameService.getGame(lobbyId).performMovement(player,movement);
        return null;
    }
}
