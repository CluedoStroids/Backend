package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.DiceResult;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Random;

@Controller
public class DiceController {

    private final SimpMessagingTemplate messagingTemplate;
    private final Random random = new Random();

    public DiceController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/rollDice")
    public void rollDice() {
        int diceOneValue = random.nextInt(6) + 1;
        int diceTwoValue = random.nextInt(6) + 1;

        DiceResult result = new DiceResult(diceOneValue, diceTwoValue);
        messagingTemplate.convertAndSend("/topic/diceResult", result);
    }


}
