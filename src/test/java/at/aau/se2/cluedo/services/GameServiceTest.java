package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.dto.SolveCaseRequest;
import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.cards.CardType;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.lobby.Lobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameServiceTest {

    @Mock
    private LobbyService lobbyService;

    @InjectMocks
    private GameService gameService;

    private Player player1;
    private Player player2;
    private Player player3;
    private Lobby testLobby;
    private final String TEST_LOBBY_ID = "test-lobby-id";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        player1 = new Player("Player1", "Player1", 0, 0, PlayerColor.RED);
        player2 = new Player("Player2", "Player2", 0, 0, PlayerColor.BLUE);
        player3 = new Player("Player3", "Player3", 0, 0, PlayerColor.GREEN);

        testLobby = new Lobby(TEST_LOBBY_ID, player1);
        testLobby.addPlayer(player2);
        testLobby.addPlayer(player3);
    }

    @Test
    void canStartGame_WithEnoughPlayers_ShouldReturnTrue() {
        when(lobbyService.getLobby(TEST_LOBBY_ID)).thenReturn(testLobby);

        boolean result = gameService.canStartGame(TEST_LOBBY_ID);

        assertTrue(result);
        verify(lobbyService).getLobby(TEST_LOBBY_ID);
    }

    @Test
    void canStartGame_WithNotEnoughPlayers_ShouldReturnFalse() {
        Lobby lobbyWithTwoPlayers = new Lobby(TEST_LOBBY_ID, player1);
        lobbyWithTwoPlayers.addPlayer(player2);

        when(lobbyService.getLobby(TEST_LOBBY_ID)).thenReturn(lobbyWithTwoPlayers);

        boolean result = gameService.canStartGame(TEST_LOBBY_ID);

        assertFalse(result);
        verify(lobbyService).getLobby(TEST_LOBBY_ID);
    }

    @Test
    void startGameFromLobby_WithEnoughPlayers_ShouldCreateGame() {
        when(lobbyService.getLobby(TEST_LOBBY_ID)).thenReturn(testLobby);

        GameManager gameManager = gameService.startGameFromLobby(TEST_LOBBY_ID);

        assertNotNull(gameManager);
        assertEquals(3, gameManager.getPlayers().size());
        verify(lobbyService).getLobby(TEST_LOBBY_ID);
    }

    @Test
    void startGameFromLobby_WithNotEnoughPlayers_ShouldThrowException() {
        Lobby lobbyWithTwoPlayers = new Lobby(TEST_LOBBY_ID, player1);
        lobbyWithTwoPlayers.addPlayer(player2);

        when(lobbyService.getLobby(TEST_LOBBY_ID)).thenReturn(lobbyWithTwoPlayers);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            gameService.startGameFromLobby(TEST_LOBBY_ID);
        });

        assertTrue(exception.getMessage().contains("Not enough players"));
        verify(lobbyService).getLobby(TEST_LOBBY_ID);
    }

    @Test
    void testCorrectSolveCaseMarksPlayerAsWinner() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player player = new Player("TestUser", "Scarlet", 0, 0, PlayerColor.RED);
        GameManager manager = new GameManager(List.of(player));

        BasicCard correctChar = new BasicCard("Miss Scarlett", null, CardType.CHARACTER);
        BasicCard correctRoom = new BasicCard("Study", null, CardType.ROOM);
        BasicCard correctWeapon = new BasicCard("Candlestick", null, CardType.WEAPON);
        SecretFile solution = new SecretFile(correctRoom, correctWeapon, correctChar);
        manager.setSecretFile(solution);

        simpleGameService.getActiveGames().put("test-lobby", manager);
        SolveCaseRequest request = new SolveCaseRequest("test-lobby", "Miss Scarlett", "Study", "Candlestick", "TestUser");
        simpleGameService.processSolveCase(request);

        Player p = simpleGameService.getGame("test-lobby").getCurrentPlayer();
        assertTrue(p.hasWon());
        assertTrue(p.isActive());
    }

    @Test
    void testWrongSolveCaseEliminatesPlayer() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player player = new Player("TestUser", "Scarlet", 0, 0, PlayerColor.RED);
        GameManager manager = new GameManager(List.of(player));

        BasicCard correctChar = new BasicCard("Miss Scarlett", null, CardType.CHARACTER);
        BasicCard correctRoom = new BasicCard("Study", null, CardType.ROOM);
        BasicCard correctWeapon = new BasicCard("Candlestick", null, CardType.WEAPON);
        SecretFile solution = new SecretFile(correctRoom, correctWeapon, correctChar);
        manager.setSecretFile(solution);

        simpleGameService.getActiveGames().put("test-lobby", manager);
        SolveCaseRequest request = new SolveCaseRequest("test-lobby", "Wrong", "Wrong", "Wrong", "TestUser");
        simpleGameService.processSolveCase(request);

        Player p = simpleGameService.getGame("test-lobby").getCurrentPlayer();
        assertFalse(p.hasWon());
        assertFalse(p.isActive());

    }
}
