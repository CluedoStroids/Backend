package at.aau.se2.cluedo.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class DiceControllerTest {

    @Test
    void rollDice_sendsTwoMessages_withValuesBetween1And6() {
        SimpMessagingTemplate mockTemplate = mock(SimpMessagingTemplate.class);
        DiceController controller = new DiceController(mockTemplate);

        controller.rollDice();

        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
        verify(mockTemplate, times(2)).convertAndSend(eq("/topic/diceResult"), captor.capture());

        for (Integer value : captor.getAllValues()) {
            assertTrue(value >= 1 && value <= 6, "Dice value should be between 1 and 6");
        }
    }
}
