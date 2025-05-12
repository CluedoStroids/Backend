package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.DiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.SecureRandom;
import java.util.Random;

@Controller
public class DiceController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final SecureRandom random = new SecureRandom();

    public DiceController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/rollDice")
    public void rollDice() {
        int diceOneValue = random.nextInt(6) + 1;
        int diceTwoValue = random.nextInt(6) + 1;

        DiceResult result = new DiceResult(diceOneValue, diceTwoValue);
        messagingTemplate.convertAndSend("/topic/diceResult", result);
        logger.info("Hey würfel mich");
    }


}
