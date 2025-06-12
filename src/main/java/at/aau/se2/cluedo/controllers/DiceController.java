package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.DiceResult;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.concurrent.ThreadLocalRandom;

@Controller
public class DiceController {

    private final SimpMessagingTemplate messagingTemplate;

    public DiceController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/rollDice")
    public void rollDice(@RequestBody(required = false) DiceResult result) {
        if (result == null || result.getDiceOne() == 0 || result.getDiceTwo() == 0) {
            int diceOneValue = ThreadLocalRandom.current().nextInt(1, 7);
            int diceTwoValue = ThreadLocalRandom.current().nextInt(1, 7);
            result = new DiceResult(diceOneValue, diceTwoValue);
        }
        
        messagingTemplate.convertAndSend("/topic/diceResult", result);
    }
}
