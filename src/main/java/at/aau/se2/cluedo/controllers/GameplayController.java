package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.*;
import at.aau.se2.cluedo.models.gameboard.GameBoard;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.services.GameService;
import at.aau.se2.cluedo.services.LobbyService;
import at.aau.se2.cluedo.services.TurnService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;


@Controller
public class GameplayController {

    private static final Logger logger = LoggerFactory.getLogger(GameplayController.class);

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private GameService gameService;

    @Autowired
    private TurnService turnService;



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

    @MessageMapping("/makeSuggestion/{lobbyId}")
    public Map<String, Object> makeSuggestion(@DestinationVariable String lobbyId, SuggestionRequest request) {

        logger.info(String.format("Received: %s %s",lobbyId,request));

        try {
            // Validate turn
            if (!turnService.isPlayerTurn(lobbyId, request.getPlayerName())) {
                return Map.of(
                    "success", false,
                    "message", "It's not your turn",
                    "lobbyId", lobbyId
                );
            }

            /*
            if (!turnService.canMakeSuggestion(lobbyId, request.getPlayerName())) {
                return Map.of(
                    "success", false,
                    "message", "Cannot make suggestion at this time",
                    "lobbyId", lobbyId
                );
            }

             */

            boolean successfullSuggestion = turnService.processSuggestion(lobbyId,request);

            logger.info("Suggestion finished Controller");

            Player player = gameService.getGame(lobbyId).getPlayer(request.getPlayerName());
            gameService.getGame(lobbyId).recordSuggestion(player, request.getSuspect(), request.getRoom(), request.getWeapon());

            return Map.of(
                    "success", successfullSuggestion,
                    "message", "smth",
                    "lobbyId", lobbyId
            );

        } catch (Exception e) {
            logger.error("Error processing suggestion in lobby {}: {}", lobbyId, e.getMessage());
            return Map.of(
                "success", false,
                "message", "Error processing suggestion",
                "lobbyId", lobbyId
            );
        }
    }

    @MessageMapping("/processSuggestion/{lobbyId}")
    public void processSuggestion(@DestinationVariable String lobbyId,SuggestionResponse suggestionResponse) {
        logger.info("Suggest: "+suggestionResponse.getCardName());
            turnService.receiveSuggestionResponse(suggestionResponse.getPlayerId(),suggestionResponse.getCardName());
    }


    @MessageMapping("/makeAccusation/{lobbyId}")
    public void makeAccusation(@DestinationVariable String lobbyId, AccusationRequest request) {
        try {
            logger.info("Accusation made in {} from user: {}", lobbyId, request.getLobbyId());

            turnService.processAccusation(
                lobbyId,
                request.getUsername(),
                request.getSuspect(),
                request.getWeapon(),
                request.getRoom()
            );
        } catch (Exception e) {
            logger.error("Error processing accusation in lobby {}: {}", lobbyId, e.getMessage());
        }
    }
}
