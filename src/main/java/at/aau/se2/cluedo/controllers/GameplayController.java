package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
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
    private LobbyService lobbyService;

    @MessageMapping("/makeSuggestion/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public String makeSuggestion(@DestinationVariable Player player, String suspect, String weapon) {
        logger.info("User {} makes a suggestion.", player.getName());
        return lobbyService.makeSuggestion(player, suspect, weapon);
    }

    @MessageMapping("/makeAccusation/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public String makeAccusation(@DestinationVariable Player player, SecretFile acusation) {
        logger.info("User {} makes a accusation.", player.getName());
        return lobbyService.makeAccusation(player, acusation);
    }

    /*@MessageMapping("/performMovement/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public int performMovement(@DestinationVariable Player player, List<String> movement) {
        logger.info("User {} makes a move.", player.getName());
        return lobbyService.performMovement(player, movement);
    }*/

    @MessageMapping("/displayGameBoard/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public String displayGameBoard(@DestinationVariable List<Player> players) {
        return lobbyService.displayGameBoard(players);
    }
}
