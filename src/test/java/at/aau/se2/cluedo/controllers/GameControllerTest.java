package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.GameStartedResponse;
import at.aau.se2.cluedo.dto.StartGameRequest;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import at.aau.se2.cluedo.models.lobby.Lobby;
import at.aau.se2.cluedo.services.GameService;
import at.aau.se2.cluedo.services.LobbyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameControllerTest {

    @Mock
    private GameService gameService;

    @Mock
    private LobbyService lobbyService;

    @Mock
    private GameManager gameManager;

    @InjectMocks
    private GameController gameController;

    private Player hostPlayer;
    private Player player2;
    private Player player3;
    private Lobby testLobby;
    private final String TEST_LOBBY_ID = "test-lobby-id";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        hostPlayer = new Player("Host", "Host", 0, 0, PlayerColor.RED);
        player2 = new Player("Player2", "Player2", 0, 0, PlayerColor.BLUE);
        player3 = new Player("Player3", "Player3", 0, 0, PlayerColor.GREEN);

        testLobby = new Lobby(TEST_LOBBY_ID, hostPlayer);
        testLobby.addPlayer(player2);
        testLobby.addPlayer(player3);
    }

    @Test
    void startGame_AsHost_WithEnoughPlayers_ShouldStartGame() {
        StartGameRequest request = new StartGameRequest(hostPlayer);
        when(lobbyService.getLobby(TEST_LOBBY_ID)).thenReturn(testLobby);
        when(gameService.canStartGame(TEST_LOBBY_ID)).thenReturn(true);
        when(gameService.startGameFromLobby(TEST_LOBBY_ID)).thenReturn(gameManager);

        List<Player> gamePlayers = new ArrayList<>();
        gamePlayers.add(hostPlayer);
        gamePlayers.add(player2);
        gamePlayers.add(player3);
        when(gameManager.getPlayers()).thenReturn(gamePlayers);

        GameStartedResponse response = gameController.startGame(TEST_LOBBY_ID, request);

        assertNotNull(response);
        assertEquals(TEST_LOBBY_ID, response.getLobbyId());
        assertEquals(3, response.getPlayers().size());
        verify(lobbyService).getLobby(TEST_LOBBY_ID);
        verify(gameService).canStartGame(TEST_LOBBY_ID);
        verify(gameService).startGameFromLobby(TEST_LOBBY_ID);
        verify(gameManager).getPlayers();
    }

    @Test
    void startGame_NotAsHost_ShouldThrowException() {

        Player nonHostPlayer = new Player("NonHost", "NonHost", 0, 0, PlayerColor.YELLOW);
        StartGameRequest request = new StartGameRequest(nonHostPlayer);

        when(lobbyService.getLobby(TEST_LOBBY_ID)).thenReturn(testLobby);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            gameController.startGame(TEST_LOBBY_ID, request);
        });

        assertTrue(exception.getMessage().contains("Player not found in lobby"));
        verify(lobbyService).getLobby(TEST_LOBBY_ID);
        verify(gameService, never()).startGameFromLobby(anyString());
    }

    @Test
    void startGame_WithNotEnoughPlayers_ShouldThrowException() {
        StartGameRequest request = new StartGameRequest(hostPlayer);
        when(lobbyService.getLobby(TEST_LOBBY_ID)).thenReturn(testLobby);
        when(gameService.canStartGame(TEST_LOBBY_ID)).thenReturn(false);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            gameController.startGame(TEST_LOBBY_ID, request);
        });

        assertTrue(exception.getMessage().contains("Not enough players"));
        verify(lobbyService).getLobby(TEST_LOBBY_ID);
        verify(gameService).canStartGame(TEST_LOBBY_ID);
        verify(gameService, never()).startGameFromLobby(anyString());
    }
}
