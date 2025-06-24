package at.aau.se2.cluedo.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccusationRequestTest {

    @Test
    void testAccusationRequestSettersAndDefaultConstructor() {
        AccusationRequest request = new AccusationRequest();

        request.setLobbyId("lobby42");
        request.setUsername("Alice");
        request.setSuspect("Scarlett");
        request.setRoom("Library");
        request.setWeapon("Rope");

        assertEquals("lobby42", request.getLobbyId());
        assertEquals("Alice", request.getUsername());
        assertEquals("Scarlett", request.getSuspect());
        assertEquals("Library", request.getRoom());
        assertEquals("Rope", request.getWeapon());
    }
}
