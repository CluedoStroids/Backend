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
    void testStartGameThrowsWhenLobbyNotFound() {
        GameService service = new GameService(new LobbyService(new LobbyRegistry()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.startGameFromLobby("non-existent");
        });

        assertEquals("Lobby not found", exception.getMessage());
    }

    @Test
    void startGameFromLobby_WithTooManyPlayers_ShouldTruncate() {
        Player p1 = new Player("P1", "Char1", 0, 0, PlayerColor.RED);
        Player p2 = new Player("P2", "Char2", 0, 0, PlayerColor.BLUE);
        Player p3 = new Player("P3", "Char3", 0, 0, PlayerColor.GREEN);
        Player p4 = new Player("P4", "Char4", 0, 0, PlayerColor.YELLOW);
        Player p5 = new Player("P5", "Char5", 0, 0, PlayerColor.PURPLE);
        Player p6 = new Player("P6", "Char6", 0, 0, PlayerColor.WHITE);
        Player p7 = new Player("P7", "Char7", 0, 0, PlayerColor.RED); // doppelte Farbe nur für Test okay

        Lobby overfilledLobby = new Lobby("overfilled-lobby", p1);
        overfilledLobby.addPlayer(p2);
        overfilledLobby.addPlayer(p3);
        overfilledLobby.addPlayer(p4);
        overfilledLobby.addPlayer(p5);
        overfilledLobby.addPlayer(p6);
        overfilledLobby.addPlayer(p7);

        when(lobbyService.getLobby("overfilled-lobby")).thenReturn(overfilledLobby);

        GameManager gameManager = gameService.startGameFromLobby("overfilled-lobby");

        assertNotNull(gameManager);
        assertEquals(6, gameManager.getPlayers().size(), "Player list should be truncated to MAX_PLAYERS = 6");
    }


    @Test
    void canStartGame_LobbyIsNull_ShouldReturnFalse() {
        when(lobbyService.getLobby("nonexistent-lobby")).thenReturn(null);

        boolean result = gameService.canStartGame("nonexistent-lobby");

        assertFalse(result);
        verify(lobbyService).getLobby("nonexistent-lobby");
    }

    @Test
    void canStartGame_NotEnoughPlayers_ShouldReturnFalse() {
        Lobby smallLobby = new Lobby("small-lobby", player1);
        smallLobby.addPlayer(player2);

        when(lobbyService.getLobby("small-lobby")).thenReturn(smallLobby);

        boolean result = gameService.canStartGame("small-lobby");

        assertFalse(result);
        verify(lobbyService).getLobby("small-lobby");
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
    void testSolveCase_RoomMismatch_PlayerEliminated() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player player = new Player("TestUser", "Scarlet", 0, 0, PlayerColor.RED);
        GameManager manager = new GameManager(List.of(player));

        BasicCard correctChar = new BasicCard("Miss Scarlett", null, CardType.CHARACTER);
        BasicCard correctRoom = new BasicCard("Study", null, CardType.ROOM);
        BasicCard correctWeapon = new BasicCard("Candlestick", null, CardType.WEAPON);
        SecretFile solution = new SecretFile(correctRoom, correctWeapon, correctChar);
        manager.setSecretFile(solution);

        simpleGameService.getActiveGames().put("test-lobby", manager);
        // Room is incorrect here
        SolveCaseRequest request = new SolveCaseRequest("test-lobby", "Miss Scarlett", "WrongRoom", "Candlestick", "TestUser");
        simpleGameService.processSolveCase(request);

        Player p = simpleGameService.getGame("test-lobby").getCurrentPlayer();
        assertFalse(p.hasWon());
        assertFalse(p.isActive());
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

    @Test
    void testProcessSolveCase_GameIsNull() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        SolveCaseRequest request = new SolveCaseRequest("nonexistent-lobby", "Any", "Any", "Any", "Any");
        assertDoesNotThrow(() -> simpleGameService.processSolveCase(request));
    }

    @Test
    void testProcessSolveCase_PlayerNotFound() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player player = new Player("SomeoneElse", "Alias", 0, 0, PlayerColor.RED);
        GameManager manager = new GameManager(List.of(player));

        simpleGameService.getActiveGames().put("test-lobby", manager);
        SolveCaseRequest request = new SolveCaseRequest("test-lobby", "Miss Scarlett", "Study", "Candlestick", "TestUser");
        assertDoesNotThrow(() -> simpleGameService.processSolveCase(request));
    }

    @Test
    void testProcessSolveCase_PlayerAlreadyEliminated() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player player = new Player("TestUser", "Alias", 0, 0, PlayerColor.RED);
        player.setActive(false);
        GameManager manager = new GameManager(List.of(player));

        simpleGameService.getActiveGames().put("test-lobby", manager);
        SolveCaseRequest request = new SolveCaseRequest("test-lobby", "Miss Scarlett", "Study", "Candlestick", "TestUser");
        assertDoesNotThrow(() -> simpleGameService.processSolveCase(request));
    }

    @Test
    void testSolveCase_WeaponMismatch_PlayerEliminated() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player player = new Player("TestUser", "Scarlet", 0, 0, PlayerColor.RED);
        GameManager manager = new GameManager(List.of(player));

        BasicCard correctChar = new BasicCard("Miss Scarlett", null, CardType.CHARACTER);
        BasicCard correctRoom = new BasicCard("Study", null, CardType.ROOM);
        BasicCard correctWeapon = new BasicCard("Candlestick", null, CardType.WEAPON);
        SecretFile solution = new SecretFile(correctRoom, correctWeapon, correctChar);
        manager.setSecretFile(solution);

        simpleGameService.getActiveGames().put("test-lobby", manager);
        SolveCaseRequest request = new SolveCaseRequest("test-lobby", "Miss Scarlett", "Study", "WrongWeapon", "TestUser");
        simpleGameService.processSolveCase(request);

        Player p = simpleGameService.getGame("test-lobby").getCurrentPlayer();
        assertFalse(p.hasWon());
        assertFalse(p.isActive());
    }
}
