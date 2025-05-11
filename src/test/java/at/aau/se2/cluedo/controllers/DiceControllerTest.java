package at.aau.se2.cluedo.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.*;

public class DiceControllerTest {

    @Test
    void rollDice_sendsTwoMessages_withValuesBetween1And6() {
        SimpMessagingTemplate mockTemplate = mock(SimpMessagingTemplate.class);
        DiceController controller = new DiceController(mockTemplate);

        controller.rollDice();

        verify(mockTemplate, times(2)).convertAndSend(eq("/topic/diceResult"), intThat(val -> val >= 1 && val <= 6));
    }

}
