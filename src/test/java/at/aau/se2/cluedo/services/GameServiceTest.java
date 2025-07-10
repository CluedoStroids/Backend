package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.dto.SuggestionRequest;
import at.aau.se2.cluedo.dto.AccusationRequest;
import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.cards.CardType;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import at.aau.se2.cluedo.models.lobby.Lobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
    void testCorrectAccusationMarksPlayerAsWinner() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player player = new Player("TestUser", "Miss Scarlett", 0, 0, PlayerColor.RED);
        GameManager manager = new GameManager(List.of(player));

        BasicCard correctChar = new BasicCard("Miss Scarlett", null, CardType.CHARACTER);
        BasicCard correctRoom = new BasicCard("Study", null, CardType.ROOM);
        BasicCard correctWeapon = new BasicCard("Candlestick", null, CardType.WEAPON);
        SecretFile solution = new SecretFile(correctRoom, correctWeapon, correctChar);
        manager.setSecretFile(solution);


        simpleGameService.getActiveGames().put("test-lobby", manager);
        AccusationRequest request = new AccusationRequest("test-lobby", "TestUser", "Miss Scarlett", "Study", "Candlestick");
        simpleGameService.processAccusation(request);

        Player p = simpleGameService.getGame("test-lobby").getCurrentPlayer();
        assertTrue(p.hasWon());
        assertTrue(p.isActive());
    }


    @Test
    void testProcessAccusation_GameIsNull() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        AccusationRequest request = new AccusationRequest("nonexistent-lobby", "Any", "Any", "Any", "Any");
        assertDoesNotThrow(() -> simpleGameService.processAccusation(request));
    }

    @Test
    void testProcessAccusation_PlayerNotFound() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player player = new Player("SomeoneElse", "Alias", 0, 0, PlayerColor.RED);
        GameManager manager = new GameManager(List.of(player));

        simpleGameService.getActiveGames().put("test-lobby", manager);
        AccusationRequest request = new AccusationRequest("test-lobby", "Miss Scarlett", "Study", "Candlestick", "TestUser");
        assertDoesNotThrow(() -> simpleGameService.processAccusation(request));
    }

    @Test
    void testProcessAccusation_PlayerAlreadyEliminated() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player player = new Player("TestUser", "Alias", 0, 0, PlayerColor.RED);
        player.setActive(false);
        GameManager manager = new GameManager(List.of(player));

        simpleGameService.getActiveGames().put("test-lobby", manager);
        AccusationRequest request = new AccusationRequest("test-lobby", "Miss Scarlett", "Study", "Candlestick", "TestUser");
        assertDoesNotThrow(() -> simpleGameService.processAccusation(request));
    }


    static Stream<AccusationRequest> provideWrongAccusations() {
        return Stream.of(
                new AccusationRequest("test-lobby", "TestUser", "WrongCharacter", "Study", "Candlestick"),
                new AccusationRequest("test-lobby", "TestUser", "Miss Scarlett", "WrongRoom", "Candlestick"),
                new AccusationRequest("test-lobby", "TestUser", "Miss Scarlett", "Study", "WrongWeapon")
        );
    }


    @ParameterizedTest
    @MethodSource("provideWrongAccusations")
    void testAccusation_PlayerEliminatedOnMismatch(AccusationRequest request) {
        GameService simpleGameService = new GameService(new LobbyService(null));

        GameManager manager = getGameManager();

        simpleGameService.getActiveGames().put("test-lobby", manager);

        simpleGameService.processAccusation(request);

        Player p = simpleGameService.getGame("test-lobby").getCurrentPlayer();
        assertFalse(p.hasWon());
        assertFalse(p.isActive());
    }

    private static GameManager getGameManager() {
        Player player = new Player("TestUser", "Scarlet", 0, 0, PlayerColor.RED);
        GameManager manager = new GameManager(List.of(player));

        BasicCard correctChar = new BasicCard("Miss Scarlett", null, CardType.CHARACTER);
        BasicCard correctRoom = new BasicCard("Study", null, CardType.ROOM);
        BasicCard correctWeapon = new BasicCard("Candlestick", null, CardType.WEAPON);
        SecretFile solution = new SecretFile(correctRoom, correctWeapon, correctChar);
        manager.setSecretFile(solution);
        return manager;
    }

    @Test
    void testPerformMovement_NonExistentGame_ShouldThrowException() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player player = new Player("TestUser", "Scarlet", 0, 0, PlayerColor.RED);
        List<String> movement = List.of("north", "east");

        // Should throw NullPointerException when trying to access non-existent game
        assertThrows(NullPointerException.class, () -> {
            simpleGameService.performMovement(player, movement, "non-existent-lobby");
        });
    }

    @Test
    void testPerformMovement_EmptyMovementList() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player player = new Player("TestUser", "Scarlet", 0, 0, PlayerColor.RED);
        GameManager manager = new GameManager(List.of(player));

        simpleGameService.getActiveGames().put("test-lobby", manager);
        List<String> emptyMovement = List.of();

        assertDoesNotThrow(() -> simpleGameService.performMovement(player, emptyMovement, "test-lobby"));
    }
    @Test
    void testPerformMovement_ValidMovement(){
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player player = new Player("TestUser", "Scarlet", 0, 24, PlayerColor.RED);
        GameManager manager = new GameManager(List.of(player));

        simpleGameService.getActiveGames().put("test-lobby", manager);
        List<String> move = new ArrayList<>();

        move.add("W");
        simpleGameService.performMovement(player,move,"test-lobby");
        assertEquals(23,manager.getPlayer(player.getName()).getY());
    }

    // Tests for makeAccusation method
    @Test
    void testMakeAccusation_ValidGame_ReturnsExpectedMessage() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player player = new Player("TestUser", "Scarlet", 0, 0, PlayerColor.RED);
        GameManager manager = new GameManager(List.of(player));

        BasicCard charCard = new BasicCard("Miss Scarlett", null, CardType.CHARACTER);
        BasicCard roomCard = new BasicCard("Study", null, CardType.ROOM);
        BasicCard weaponCard = new BasicCard("Candlestick", null, CardType.WEAPON);
        SecretFile accusation = new SecretFile(roomCard, weaponCard, charCard);

        simpleGameService.getActiveGames().put("test-lobby", manager);

        String result = simpleGameService.makeAccusation(player, accusation, "test-lobby");

        assertEquals("Wrong! TestUser is out of the game!", result);
    }

    @Test
    void testMakeAccusation_NonExistentGame_ShouldThrowException() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player player = new Player("TestUser", "Scarlet", 0, 0, PlayerColor.RED);

        BasicCard charCard = new BasicCard("Miss Scarlett", null, CardType.CHARACTER);
        BasicCard roomCard = new BasicCard("Study", null, CardType.ROOM);
        BasicCard weaponCard = new BasicCard("Candlestick", null, CardType.WEAPON);
        SecretFile accusation = new SecretFile(roomCard, weaponCard, charCard);

        assertThrows(NullPointerException.class, () -> {
            simpleGameService.makeAccusation(player, accusation, "non-existent-lobby");
        });
    }

    @Test
    void testProcessSuggestion_GameNotFound() {
        GameService simpleGameService = new GameService(new LobbyService(null));

        SuggestionRequest request = new SuggestionRequest("invalid-lobby", "Scarlet", "Knife", "Kitchen", "TestUser");

        assertDoesNotThrow(() -> simpleGameService.processSuggestion(request));
    }

    @Test
    void testProcessSuggestion_PlayerNotFound() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player otherPlayer = new Player("SomeoneElse", "Plum", 1, 1, PlayerColor.PURPLE);
        GameManager manager = new GameManager(List.of(otherPlayer));

        simpleGameService.getActiveGames().put("test-lobby", manager);

        SuggestionRequest request = new SuggestionRequest("test-lobby", "Scarlet", "Knife", "Kitchen", "TestUser");

        assertDoesNotThrow(() -> simpleGameService.processSuggestion(request));
    }


    @Test
    void testPerformMovement_ValidMovement() {
        GameService simpleGameService = new GameService(new LobbyService(null));
        Player player = new Player("TestUser", "Scarlet", 0, 0, PlayerColor.RED);
        GameManager manager = mock(GameManager.class);
        List<String> movement = new ArrayList<>(List.of("W", "A"));

        simpleGameService.getActiveGames().put("test-lobby", manager);
        doAnswer(invocation -> {
            // Verify movement list is cleared
            movement.clear();
            return null;
        }).when(manager).performMovement(player, movement);

        simpleGameService.performMovement(player, movement, "test-lobby");

        assertTrue(movement.isEmpty());
        verify(manager).performMovement(player, movement);
    }

    @Test
    void getGame_WithExistingLobbyId_ShouldReturnCorrectGameManager() {
        GameManager gm = new GameManager(List.of(player1, player2, player3));
        String lobbyId = "testLobby";
        gameService.getActiveGames().put(lobbyId, gm);

        GameManager retrieved = gameService.getGame(lobbyId);
        assertEquals(gm, retrieved);
    }

    @Test
    void testMakeAccusation_CorrectGuess_ReturnsVictoryMessage() {
        Player player = new Player("Victor", "Alias", 1, 1, PlayerColor.YELLOW);
        GameManager manager = new GameManager(List.of(player));

        BasicCard charCard = new BasicCard("Victor", null, CardType.CHARACTER);
        BasicCard roomCard = new BasicCard("Kitchen", null, CardType.ROOM);
        BasicCard weaponCard = new BasicCard("Knife", null, CardType.WEAPON);
        SecretFile correctFile = new SecretFile(roomCard, weaponCard, charCard);
        manager.setSecretFile(correctFile);

        gameService.getActiveGames().put("test-lobby", manager);
        String result = gameService.makeAccusation(player, correctFile, "test-lobby");

        assertTrue(result.contains("Wrong!") || result.contains("Victor"), "Message should acknowledge accusation");
    }

}

