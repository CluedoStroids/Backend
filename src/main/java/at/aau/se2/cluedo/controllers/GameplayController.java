package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.models.gameboard.GameBoard;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import at.aau.se2.cluedo.services.GameService;
import at.aau.se2.cluedo.services.LobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;


@Controller
public class GameplayController {


    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private GameService gameService;

    @MessageMapping("/makeSuggestion/{lobbyId}")
    @SendTo("/topic/madeSuggestion/{lobbyId}")
    public String makeSuggestion(@DestinationVariable String lobbyId, Player player, String suspect, String weapon) {

        gameService.getGame(lobbyId).makeSuggestion(player, suspect, weapon);
        return lobbyId;
    }

    @MessageMapping("/makeAccusation/{lobbyId}")
    @SendTo("/topic/madeAccusation/{lobbyId}")
    public String makeAccusation(@DestinationVariable String lobbyId, Player player, SecretFile accusation) {
        return gameService.makeAccusation(player, accusation,lobbyId);
    }

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
}
