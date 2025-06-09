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



    @MessageMapping("/makeSuggestion/{lobbyId}")
    @SendTo("/topic/suggestionMade/{lobbyId}")
    public Map<String, Object> makeSuggestion(@DestinationVariable String lobbyId, SuggestionRequest request) {
        try {
            // Validate turn
            if (!turnService.isPlayerTurn(lobbyId, request.getPlayerName())) {
                return Map.of(
                    "success", false,
                    "message", "It's not your turn",
                    "lobbyId", lobbyId
                );
            }

            if (!turnService.canMakeSuggestion(lobbyId, request.getPlayerName())) {
                return Map.of(
                    "success", false,
                    "message", "Cannot make suggestion at this time",
                    "lobbyId", lobbyId
                );
            }

            boolean success = turnService.processSuggestion(
                lobbyId,
                request.getPlayerName(),
                request.getSuspect(),
                request.getWeapon()
            );

            if (!success) {
                return Map.of(
                    "success", false,
                    "message", "Invalid suggestion attempt",
                    "lobbyId", lobbyId
                );
            }

            return Map.of(
                "success", true,
                "player", request.getPlayerName(),
                "suspect", request.getSuspect(),
                "weapon", request.getWeapon(),
                "room", request.getRoom(),
                "message", request.getPlayerName() + " suggests " + request.getSuspect() + " with " + request.getWeapon() + " in " + request.getRoom(),
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


    @MessageMapping("/makeAccusation/{lobbyId}")
    @SendTo("/topic/accusationMade/{lobbyId}")
    public Map<String, Object> makeAccusation(@DestinationVariable String lobbyId, AccusationRequest request) {
        try {
            // Validate turn
            if (!turnService.isPlayerTurn(lobbyId, request.getPlayerName())) {
                return Map.of(
                    "success", false,
                    "message", "It's not your turn",
                    "lobbyId", lobbyId
                );
            }

            if (!turnService.canMakeAccusation(lobbyId, request.getPlayerName())) {
                return Map.of(
                    "success", false,
                    "message", "Cannot make accusation at this time",
                    "lobbyId", lobbyId
                );
            }

            boolean success = turnService.processAccusation(
                lobbyId,
                request.getPlayerName(),
                request.getSuspect(),
                request.getWeapon(),
                request.getRoom()
            );

            return Map.of(
                "success", success,
                "player", request.getPlayerName(),
                "suspect", request.getSuspect(),
                "weapon", request.getWeapon(),
                "room", request.getRoom(),
                "message", request.getPlayerName() + " accuses " + request.getSuspect() + " with " + request.getWeapon() + " in " + request.getRoom(),
                "lobbyId", lobbyId
            );
        } catch (Exception e) {
            logger.error("Error processing accusation in lobby {}: {}", lobbyId, e.getMessage());
            return Map.of(
                "success", false,
                "message", "Error processing accusation",
                "lobbyId", lobbyId
            );
        }
    }
}
