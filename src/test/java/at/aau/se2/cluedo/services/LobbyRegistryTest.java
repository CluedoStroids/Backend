package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.lobby.Lobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LobbyRegistryTest {

    private LobbyRegistry lobbyRegistry;
    private Player hostPlayer;

    @BeforeEach
    void setUp() {
        lobbyRegistry = new LobbyRegistry();
        hostPlayer = new Player("testHost", "Red", 0, 0);
    }

    @Test
    void createLobby_ShouldCreateNewLobbyWithHost() {
        Lobby lobby = lobbyRegistry.createLobby(hostPlayer);

        assertNotNull(lobby);
        assertNotNull(lobby.getId());
        assertEquals(hostPlayer.getPlayerID(), lobby.getHostId());
        assertEquals(hostPlayer.getName(), lobby.getHost().getName());
        assertTrue(lobby.getPlayers().contains(hostPlayer));
        assertEquals(1, lobby.getPlayers().size());
    }

    @Test
    void getLobby_WithValidId_ShouldReturnLobby() {
        Lobby createdLobby = lobbyRegistry.createLobby(hostPlayer);
        String lobbyId = createdLobby.getId();
        Lobby retrievedLobby = lobbyRegistry.getLobby(lobbyId);

        assertNotNull(retrievedLobby);
        assertEquals(lobbyId, retrievedLobby.getId());
        assertEquals(hostPlayer.getPlayerID(), retrievedLobby.getHostId());
        assertEquals(hostPlayer.getName(), retrievedLobby.getHost().getName());
    }


    @Test
    void removeLobby_WithValidId_ShouldRemoveLobby() {
        Lobby lobby = lobbyRegistry.createLobby(hostPlayer);
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
