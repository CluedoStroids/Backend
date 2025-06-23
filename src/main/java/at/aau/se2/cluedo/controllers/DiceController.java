package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.DiceResult;
import at.aau.se2.cluedo.dto.TurnActionRequest;
import at.aau.se2.cluedo.dto.TurnStateResponse;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.services.GameService;
import at.aau.se2.cluedo.services.TurnService;
import at.aau.se2.cluedo.services.TurnService.TurnState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.concurrent.ThreadLocalRandom;

@Controller
public class DiceController {

    private static final Logger logger = LoggerFactory.getLogger(DiceController.class);
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TurnService turnService;

    @Autowired
    private GameService gameService;

    public DiceController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/rollDice")
    public void rollDice() {
        int diceOneValue = ThreadLocalRandom.current().nextInt(1, 7);
        int diceTwoValue = ThreadLocalRandom.current().nextInt(1, 7);

        DiceResult result = new DiceResult(diceOneValue, diceTwoValue);
        messagingTemplate.convertAndSend("/topic/diceResult", result);
    }

    /**
     * Roll dice for a player's turn
     * @param lobbyId
     * @param request
     * @return TurnStateResponse
     */
    @MessageMapping("/rollDice/{lobbyId}")
    public TurnStateResponse rollDiceForTurn(@DestinationVariable String lobbyId, TurnActionRequest request) {
        try {
            if (turnService.getTurnState(lobbyId) != TurnState.PLAYERS_TURN_ROLL_DICE) {
                return createErrorResponse(lobbyId, "Invalid turn state for dice roll");
            }

            // generate dice value if not provided
            int diceValue = request.getDiceValue() > 0 ? request.getDiceValue() :
                    ThreadLocalRandom.current().nextInt(1, 7) + ThreadLocalRandom.current().nextInt(1, 7);

            boolean success = turnService.processDiceRoll(lobbyId, request.getPlayerName(), diceValue);

            if (!success) {
                return createErrorResponse(lobbyId, "Invalid dice roll attempt");
            }

            GameManager game = gameService.getGame(lobbyId);
            Player currentPlayer = game.getCurrentPlayer();

            return new TurnStateResponse(
                lobbyId,
                currentPlayer.getName(),
                game.getCurrentPlayerIndex(),
                TurnState.PLAYERS_TURN_MOVE,
                false, // Cannot make suggestion while moving
                true,  // Can make accusation during turn
                diceValue,
                currentPlayer.getName() + " rolled " + diceValue + ". Time to move!"
            );
        } catch (Exception e) {
            logger.error("Error processing dice roll in lobby {}: {}", lobbyId, e.getMessage());
            return createErrorResponse(lobbyId, "Error processing dice roll");
        }
    }

    private TurnStateResponse createErrorResponse(String lobbyId, String message) {
        return new TurnStateResponse(
            lobbyId,
            "",
            -1,
            TurnState.PLAYER_HAS_WON,
            false,
            false,
            0,
            message
        );
    }
}
