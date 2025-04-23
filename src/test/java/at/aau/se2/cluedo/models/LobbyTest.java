package at.aau.se2.cluedo.models;

import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.lobby.Lobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class LobbyTest {

    private final String TEST_LOBBY_ID = "test-lobby-id";
    private Player hostPlayer;
    private Player joinPlayer;

    @BeforeEach
    void setUp() {
        hostPlayer = new Player("testHost", "Red", 0, 0);
        joinPlayer = new Player("testPlayer", "Blue", 0, 0);
    }

    @Test
    void constructor_ShouldInitializeLobbyWithHostAsParticipant() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, hostPlayer);

        assertEquals(TEST_LOBBY_ID, lobby.getId());
        assertEquals(hostPlayer.getPlayerID(), lobby.getHostId());
        assertEquals(hostPlayer.getName(), lobby.getHost().getName());
        assertTrue(lobby.getPlayers().contains(hostPlayer));
        assertEquals(1, lobby.getPlayers().size());
    }

    @Test
    void addPlayer_ShouldAddPlayerToLobby() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, hostPlayer);
        boolean result = lobby.addPlayer(joinPlayer);

        assertTrue(result);
        assertTrue(lobby.getPlayers().contains(joinPlayer));
        assertEquals(2, lobby.getPlayers().size());
    }

    @Test
    void addPlayer_WhenPlayerAlreadyInLobby_ShouldReturnFalse() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, hostPlayer);
        lobby.addPlayer(joinPlayer);
        boolean result = lobby.addPlayer(joinPlayer);

        assertFalse(result);
        assertEquals(2, lobby.getPlayers().size());
    }

    @Test
    void removePlayer_ShouldRemovePlayerFromLobby() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, hostPlayer);
        lobby.addPlayer(joinPlayer);
        boolean result = lobby.removePlayer(joinPlayer);

        assertTrue(result);
        assertFalse(lobby.getPlayers().contains(joinPlayer));
        assertEquals(1, lobby.getPlayers().size());
    }

    @Test
    void removePlayer_WhenPlayerNotInLobby_ShouldReturnFalse() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, hostPlayer);
        Player nonExistingPlayer = new Player("nonExisting", "Green", 0, 0);
        boolean result = lobby.removePlayer(nonExistingPlayer);

        assertFalse(result);
        assertEquals(1, lobby.getPlayers().size());
    }

    @Test
    void hasPlayer_WhenPlayerInLobby_ShouldReturnTrue() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, hostPlayer);
        lobby.addPlayer(joinPlayer);
        boolean result = lobby.hasPlayer(joinPlayer);

        assertTrue(result);
    }

    @Test
    void hasPlayer_WhenPlayerNotInLobby_ShouldReturnFalse() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, hostPlayer);
        Player nonExistingPlayer = new Player("nonExisting", "Green", 0, 0);
        boolean result = lobby.hasPlayer(nonExistingPlayer);

        assertFalse(result);
    }

    @Test
    void getParticipantNames_ShouldReturnListOfPlayerNames() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, hostPlayer);
        lobby.addPlayer(joinPlayer);

        assertEquals(2, lobby.getParticipantNames().size());
        assertTrue(lobby.getParticipantNames().contains(hostPlayer.getName()));
        assertTrue(lobby.getParticipantNames().contains(joinPlayer.getName()));
    }
}
