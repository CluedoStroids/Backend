package at.aau.se2.cluedo.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class LobbyTest {

    private final String TEST_LOBBY_ID = "test-lobby-id";
    private final String HOST_USERNAME = "testHost";
    private final String PLAYER_USERNAME = "testPlayer";

    @Test
    void constructor_ShouldInitializeLobbyWithHostAsParticipant() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, HOST_USERNAME);

        assertEquals(TEST_LOBBY_ID, lobby.getId());
        assertEquals(HOST_USERNAME, lobby.getHost());
        assertTrue(lobby.getParticipants().contains(HOST_USERNAME));
        assertEquals(1, lobby.getParticipants().size());
    }

    @Test
    void addParticipant_ShouldAddUserToParticipants() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, HOST_USERNAME);
        boolean result = lobby.addParticipant(PLAYER_USERNAME);

        assertTrue(result);
        assertTrue(lobby.getParticipants().contains(PLAYER_USERNAME));
        assertEquals(2, lobby.getParticipants().size());
    }

    @Test
    void addParticipant_WhenUserAlreadyInLobby_ShouldReturnFalse() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, HOST_USERNAME);
        lobby.addParticipant(PLAYER_USERNAME);
        boolean result = lobby.addParticipant(PLAYER_USERNAME);

        assertFalse(result);
        assertEquals(2, lobby.getParticipants().size());
    }

    @Test
    void removeParticipant_ShouldRemoveUserFromParticipants() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, HOST_USERNAME);
        lobby.addParticipant(PLAYER_USERNAME);
        boolean result = lobby.removeParticipant(PLAYER_USERNAME);

        assertTrue(result);
        assertFalse(lobby.getParticipants().contains(PLAYER_USERNAME));
        assertEquals(1, lobby.getParticipants().size());
    }

    @Test
    void removeParticipant_WhenUserNotInLobby_ShouldReturnFalse() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, HOST_USERNAME);
        boolean result = lobby.removeParticipant(PLAYER_USERNAME);

        assertFalse(result);
        assertEquals(1, lobby.getParticipants().size());
    }

    @Test
    void hasParticipant_WhenUserInLobby_ShouldReturnTrue() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, HOST_USERNAME);
        lobby.addParticipant(PLAYER_USERNAME);
        boolean result = lobby.hasParticipant(PLAYER_USERNAME);

        assertTrue(result);
    }

    @Test
    void hasParticipant_WhenUserNotInLobby_ShouldReturnFalse() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, HOST_USERNAME);
        boolean result = lobby.hasParticipant(PLAYER_USERNAME);

        assertFalse(result);
    }
}
