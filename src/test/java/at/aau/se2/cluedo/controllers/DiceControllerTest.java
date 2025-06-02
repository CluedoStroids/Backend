package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.DiceResult;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiceControllerTest {

    static class FakeSimpMessagingTemplate extends SimpMessagingTemplate {
        List<Object> sentMessages = new ArrayList<>();
        List<String> destinations = new ArrayList<>();

        public FakeSimpMessagingTemplate() {
            super(new StubMessageChannel()); // Provide a stub that satisfies the constructor
        }

        @Override
        public void convertAndSend(String destination, Object payload) {
            destinations.add(destination);
            sentMessages.add(payload);
        }
    }

    static class StubMessageChannel implements org.springframework.messaging.MessageChannel {
        @Override
        public boolean send(org.springframework.messaging.Message<?> message) {
            return true;
        }

        @Override
        public boolean send(org.springframework.messaging.Message<?> message, long timeout) {
            return true;
        }
    }

    @Test
    void testRollDice() {
        FakeSimpMessagingTemplate fakeTemplate = new FakeSimpMessagingTemplate();
        DiceController controller = new DiceController(fakeTemplate);

        controller.rollDice();

        assertEquals(1, fakeTemplate.destinations.size());
        assertEquals("/topic/diceResult", fakeTemplate.destinations.get(0));

        assertEquals(1, fakeTemplate.sentMessages.size());
        Object payload = fakeTemplate.sentMessages.get(0);
        assertTrue(payload instanceof DiceResult);

        DiceResult result = (DiceResult) payload;
        assertTrue(result.getDiceOne() >= 1 && result.getDiceOne() <= 6);
        assertTrue(result.getDiceTwo() >= 1 && result.getDiceTwo() <= 6);
    }
}
