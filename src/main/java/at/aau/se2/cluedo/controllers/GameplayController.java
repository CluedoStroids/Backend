package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.GameStartedResponse;
import at.aau.se2.cluedo.models.gameboard.GameBoard;
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
    private LobbyService lobbyService;

    @Autowired
    private GameService gameService;

    @MessageMapping("/makeSuggestion/{lobbyId}")
    @SendTo("/topic/madeSuggestion/{lobbyId}")
    public String makeSuggestion(@DestinationVariable String lobbyId, Player player, String suspect, String weapon) {
        logger.info("User {} makes a suggestion.", player.getName());
        gameService.getGame(lobbyId).makeSuggestion(player, suspect, weapon);
        return lobbyId;
    }

    @MessageMapping("/makeAccusation/{lobbyId}")
    @SendTo("/topic/madeAccusation/{lobbyId}")
    public String makeAccusation(@DestinationVariable String lobbyId, Player player, SecretFile accusation) {
        logger.info("User {} makes a accusation.", player.getName());
        return lobbyService.makeAccusation(player, accusation);
    }

    /*@MessageMapping("/performMovement/{lobbyId}")
    @SendTo("/topic/lobby/{lobbyId}")
    public int performMovement(@DestinationVariable Player player, List<String> movement) {
        logger.info("User {} makes a move.", player.getName());
        return lobbyService.performMovement(player, movement);
        gameService.getGame(lobbyId).makeAccusation(player, accusation);

    }*/


    @MessageMapping("/displayGameBoard/{lobbyId}")
    @SendTo("/topic/displayedGameBoard/{lobbyId}")
    public String displayGameBoard(@DestinationVariable String lobbyId, List<Player> players) {
        gameService.getGame(lobbyId).getGameBoard().displayGameBoard(players);
        return lobbyId;
    }
    @MessageMapping("/getGameBoard/{lobbyId}")
    @SendTo("/topic/gotGameBoard/{lobbyId}")
    public GameBoard getGameBoard(@DestinationVariable String lobbyId) {
        return gameService.getGame(lobbyId).getGameBoard();
    }
    /*
    @MessageMapping("/performMovement/{lobbyId}")
    @SendTo("/topic/performedMovement/{lobbyId}")
    public GameStartedResponse performMovement(@DestinationVariable String lobbyId, Player player, List<String> movement) {
        logger.info("Player {} is attempting to start a movement", player.getName());
        gameService.getGame(lobbyId).performMovement(player,movement);
        return null;
    }
  */
}
