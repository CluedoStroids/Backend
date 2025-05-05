package at.aau.se2.cluedo.controllers;

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
        int diceValue = random.nextInt(11) + 2; //random number between 2 and 12
        messagingTemplate.convertAndSend("/topic/diceResult", diceValue);
    }
}
