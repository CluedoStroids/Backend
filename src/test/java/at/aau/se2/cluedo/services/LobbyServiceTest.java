package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.models.lobby.Lobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LobbyServiceTest {

    @Mock
    private LobbyRegistry lobbyRegistry;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private LobbyService lobbyService;

    private final String HOST_USERNAME = "testHost";
    private final String PLAYER_USERNAME = "testPlayer";
    private final String TEST_LOBBY_ID = "test-lobby-id";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lobbyService = new LobbyService(lobbyRegistry, messagingTemplate);
    }

    @Test
    void createLobby_ShouldCreateNewLobbyWithHost() {
        Lobby mockLobby = new Lobby(TEST_LOBBY_ID, HOST_USERNAME);
        when(lobbyRegistry.createLobby(HOST_USERNAME)).thenReturn(mockLobby);
        String lobbyId = lobbyService.createLobby(HOST_USERNAME);

        assertEquals(TEST_LOBBY_ID, lobbyId);
        verify(lobbyRegistry).createLobby(HOST_USERNAME);
    }

    @Test
    void joinLobby_ShouldAddUserToLobby() {
        Lobby mockLobby = new Lobby(TEST_LOBBY_ID, HOST_USERNAME);
        when(lobbyRegistry.getLobby(TEST_LOBBY_ID)).thenReturn(mockLobby);
        lobbyService.joinLobby(TEST_LOBBY_ID, PLAYER_USERNAME);

        assertTrue(mockLobby.getParticipants().contains(PLAYER_USERNAME));
        verify(lobbyRegistry).getLobby(TEST_LOBBY_ID);
    }

    @Test
    void leaveLobby_ShouldRemoveUserFromLobby() {
        Lobby mockLobby = new Lobby(TEST_LOBBY_ID, HOST_USERNAME);
        mockLobby.addParticipant(PLAYER_USERNAME);
        when(lobbyRegistry.getLobby(TEST_LOBBY_ID)).thenReturn(mockLobby);
        lobbyService.leaveLobby(TEST_LOBBY_ID, PLAYER_USERNAME);

        assertFalse(mockLobby.getParticipants().contains(PLAYER_USERNAME));
        verify(lobbyRegistry).getLobby(TEST_LOBBY_ID);
    }

    @Test
    void getLobby_WithValidId_ShouldReturnLobby() {
        Lobby mockLobby = new Lobby(TEST_LOBBY_ID, HOST_USERNAME);
        when(lobbyRegistry.getLobby(TEST_LOBBY_ID)).thenReturn(mockLobby);
        Lobby lobby = lobbyService.getLobby(TEST_LOBBY_ID);

        assertNotNull(lobby);
        assertEquals(TEST_LOBBY_ID, lobby.getId());
        assertEquals(HOST_USERNAME, lobby.getHost());
        verify(lobbyRegistry).getLobby(TEST_LOBBY_ID);
    }
}
