package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import at.aau.se2.cluedo.models.lobby.Lobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LobbyServiceTest {

    @Mock
    private LobbyRegistry lobbyRegistry;

    private LobbyService lobbyService;
    private Player hostPlayer;
    private Player joinPlayer;
    private final String TEST_LOBBY_ID = "test-lobby-id";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lobbyService = new LobbyService(lobbyRegistry);
        hostPlayer = new Player("testHost", "Host", 0, 0, PlayerColor.RED);
        joinPlayer = new Player("testPlayer", "Join", 0, 0, PlayerColor.BLUE);
    }

    @Test
    void createLobby_ShouldCreateNewLobbyWithHost() {
        Lobby mockLobby = new Lobby(TEST_LOBBY_ID, hostPlayer);
        when(lobbyRegistry.createLobby(hostPlayer)).thenReturn(mockLobby);
        String lobbyId = lobbyService.createLobby(hostPlayer);

        assertEquals(TEST_LOBBY_ID, lobbyId);
        verify(lobbyRegistry).createLobby(hostPlayer);
    }

    @Test
    void joinLobby_ShouldAddPlayerToLobby() {
        Lobby mockLobby = new Lobby(TEST_LOBBY_ID, hostPlayer);
        when(lobbyRegistry.getLobby(TEST_LOBBY_ID)).thenReturn(mockLobby);
        lobbyService.joinLobby(TEST_LOBBY_ID, joinPlayer);

        assertTrue(mockLobby.getPlayers().contains(joinPlayer));
        verify(lobbyRegistry).getLobby(TEST_LOBBY_ID);
    }


    @Test
    void leaveLobby_ShouldRemovePlayerFromLobby() {
        Lobby mockLobby = new Lobby(TEST_LOBBY_ID, hostPlayer);
        mockLobby.addPlayer(joinPlayer);
        when(lobbyRegistry.getLobby(TEST_LOBBY_ID)).thenReturn(mockLobby);
        lobbyService.leaveLobby(TEST_LOBBY_ID, joinPlayer);

        assertFalse(mockLobby.getPlayers().contains(joinPlayer));
        verify(lobbyRegistry).getLobby(TEST_LOBBY_ID);
    }

    @Test
    void getLobby_WithValidId_ShouldReturnLobby() {
        Lobby mockLobby = new Lobby(TEST_LOBBY_ID, hostPlayer);
        when(lobbyRegistry.getLobby(TEST_LOBBY_ID)).thenReturn(mockLobby);
        Lobby lobby = lobbyService.getLobby(TEST_LOBBY_ID);

        assertNotNull(lobby);
        assertEquals(TEST_LOBBY_ID, lobby.getId());
        assertEquals(hostPlayer.getPlayerID(), lobby.getHostId());
        assertEquals(hostPlayer.getName(), lobby.getHost().getName());
        verify(lobbyRegistry).getLobby(TEST_LOBBY_ID);
    }

    @Test
    void getAllActiveLobbies_ShouldReturnAllLobbies() {
        // Arrange
        Lobby lobby1 = new Lobby("lobby-id-1", hostPlayer);
        Lobby lobby2 = new Lobby("lobby-id-2", joinPlayer);
        List<Lobby> expectedLobbies = Arrays.asList(lobby1, lobby2);

        when(lobbyRegistry.getAllLobbies()).thenReturn(expectedLobbies);

        // Act
        List<Lobby> actualLobbies = lobbyService.getAllActiveLobbies();

        // Assert
        assertEquals(expectedLobbies.size(), actualLobbies.size());
        assertEquals(expectedLobbies, actualLobbies);
        verify(lobbyRegistry).getAllLobbies();
    }
}
