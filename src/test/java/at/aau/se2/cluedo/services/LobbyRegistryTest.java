package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.lobby.Lobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    void getAllLobbies_ShouldReturnAllLobbies() {
        // Create multiple lobbies
        Player host1 = new Player("Host1", "Red", 0, 0);
        Player host2 = new Player("Host2", "Blue", 0, 0);

        Lobby lobby1 = lobbyRegistry.createLobby(host1);
        Lobby lobby2 = lobbyRegistry.createLobby(host2);

        // Get all lobbies
        List<Lobby> lobbies = lobbyRegistry.getAllLobbies();

        // Verify
        assertEquals(2, lobbies.size());
        assertTrue(lobbies.contains(lobby1));
        assertTrue(lobbies.contains(lobby2));
    }
}
