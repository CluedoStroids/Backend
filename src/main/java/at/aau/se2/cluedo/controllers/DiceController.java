package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.DiceResult;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.concurrent.ThreadLocalRandom;

@Controller
public class DiceController {

    private final SimpMessagingTemplate messagingTemplate;

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
}
