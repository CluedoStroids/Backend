package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.models.lobby.Lobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LobbyRegistryTest {

    private LobbyRegistry lobbyRegistry;
    private final String HOST_USERNAME = "testHost";

    @BeforeEach
    void setUp() {
        lobbyRegistry = new LobbyRegistry();
    }

    @Test
    void createLobby_ShouldCreateNewLobbyWithHost() {
        Lobby lobby = lobbyRegistry.createLobby(HOST_USERNAME);

        assertNotNull(lobby);
        assertNotNull(lobby.getId());
        assertEquals(HOST_USERNAME, lobby.getHost());
        assertTrue(lobby.getParticipants().contains(HOST_USERNAME));
        assertEquals(1, lobby.getParticipants().size());
    }

    @Test
    void getLobby_WithValidId_ShouldReturnLobby() {
        Lobby createdLobby = lobbyRegistry.createLobby(HOST_USERNAME);
        String lobbyId = createdLobby.getId();
        Lobby retrievedLobby = lobbyRegistry.getLobby(lobbyId);

        assertNotNull(retrievedLobby);
        assertEquals(lobbyId, retrievedLobby.getId());
        assertEquals(HOST_USERNAME, retrievedLobby.getHost());
    }


    @Test
    void removeLobby_WithValidId_ShouldRemoveLobby() {
        Lobby lobby = lobbyRegistry.createLobby(HOST_USERNAME);
        String lobbyId = lobby.getId();
        boolean result = lobbyRegistry.removeLobby(lobbyId);

        assertTrue(result);
    }

    @Test
    void removeLobby_WithInvalidId_ShouldReturnFalse() {
        boolean result = lobbyRegistry.removeLobby("invalid-id");

        assertFalse(result);
    }
}
