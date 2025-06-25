package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.cards.CardType;
import at.aau.se2.cluedo.models.gameboard.GameBoard;
import at.aau.se2.cluedo.models.gameboard.GameBoardCell;
import at.aau.se2.cluedo.models.gameboard.Room;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gamemanager.GameState;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import at.aau.se2.cluedo.services.TurnService.TurnState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TurnServiceTest {

    private GameService gameService;
    private TurnService turnService;

    private static final String TEST_LOBBY_ID = "test-lobby";
    private static final String TEST_PLAYER_NAME = "TestPlayer";
    private static final String TEST_PLAYER_NAME_2 = "TestPlayer2";

    private Player testPlayer;
    private Player testPlayer2;
    private GameManager mockGameManager;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private GameBoard mockGameBoard;
    @Mock
    private GameBoardCell mockCell;
    @Mock
    private Room mockRoom;

    @BeforeEach
    void setUp() {
        gameService = mock(GameService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        turnService = new TurnService();

        try {
            java.lang.reflect.Field gameServiceField = TurnService.class.getDeclaredField("gameService");
            gameServiceField.setAccessible(true);
            gameServiceField.set(turnService, gameService);

            java.lang.reflect.Field messagingTemplateField = TurnService.class.getDeclaredField("messagingTemplate");
            messagingTemplateField.setAccessible(true);
            messagingTemplateField.set(turnService, messagingTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        testPlayer = mock(Player.class);
        testPlayer2 = mock(Player.class);
        mockGameManager = mock(GameManager.class);
        mockGameBoard = mock(GameBoard.class);
        mockCell = mock(GameBoardCell.class);
        mockRoom = mock(Room.class);

        when(testPlayer.getName()).thenReturn(TEST_PLAYER_NAME);
        when(testPlayer2.getName()).thenReturn(TEST_PLAYER_NAME_2);
        when(testPlayer.getX()).thenReturn(5);
        when(testPlayer.getY()).thenReturn(5);

        when(mockGameManager.getGameBoard()).thenReturn(mockGameBoard);
        when(mockGameBoard.getCell(anyInt(), anyInt())).thenReturn(mockCell);
        when(mockCell.getRoom()).thenReturn(mockRoom);
        when(mockRoom.getName()).thenReturn("Test Room");
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);
    }

    @Test
    void testInitializeLobbyState() {
        turnService.initializeLobbyState(TEST_LOBBY_ID);
        assertEquals(TurnState.WAITING_FOR_PLAYERS, turnService.getTurnState(TEST_LOBBY_ID));
    }

    @Test
    void testSetWaitingForStart() {
        turnService.initializeLobbyState(TEST_LOBBY_ID);
        turnService.setWaitingForStart(TEST_LOBBY_ID);
        assertEquals(TurnState.WAITING_FOR_START, turnService.getTurnState(TEST_LOBBY_ID));
    }

    @Test
    void testSetWaitingForStartFromWrongState() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);
        when(mockGameManager.getCurrentPlayerIndex()).thenReturn(0);

        turnService.initializeTurnState(TEST_LOBBY_ID);
        turnService.setWaitingForStart(TEST_LOBBY_ID);

        assertEquals(TurnState.PLAYERS_TURN_ROLL_DICE, turnService.getTurnState(TEST_LOBBY_ID));
    }

    @Test
    void testSetWaitingForPlayers() {
        turnService.initializeLobbyState(TEST_LOBBY_ID);
        turnService.setWaitingForStart(TEST_LOBBY_ID);
        turnService.setWaitingForPlayers(TEST_LOBBY_ID);
        assertEquals(TurnState.WAITING_FOR_PLAYERS, turnService.getTurnState(TEST_LOBBY_ID));
    }

    @Test
    void testInitializeTurnState() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);
        when(mockGameManager.getCurrentPlayerIndex()).thenReturn(0);

        turnService.initializeTurnState(TEST_LOBBY_ID);
        assertEquals(TurnState.PLAYERS_TURN_ROLL_DICE, turnService.getTurnState(TEST_LOBBY_ID));
    }

    @Test
    void testGetTurnStateForNonExistentLobby() {
        TurnState state = turnService.getTurnState("non-existent");
        assertEquals(TurnState.WAITING_FOR_PLAYERS, state);
    }

    @Test
    void testIsPlayerTurn_CorrectPlayer() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);

        boolean result = turnService.isPlayerTurn(TEST_LOBBY_ID, TEST_PLAYER_NAME);
        assertTrue(result);
    }

    @Test
    void testIsPlayerTurn_WrongPlayer() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);

        boolean result = turnService.isPlayerTurn(TEST_LOBBY_ID, "WrongPlayer");
        assertFalse(result);
    }

    @Test
    void testProcessDiceRoll() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);
        when(mockGameManager.getCurrentPlayerIndex()).thenReturn(0);

        turnService.initializeTurnState(TEST_LOBBY_ID);

        boolean result = turnService.processDiceRoll(TEST_LOBBY_ID, TEST_PLAYER_NAME, 6);

        assertTrue(result);
        assertEquals(TurnState.PLAYERS_TURN_MOVE, turnService.getTurnState(TEST_LOBBY_ID));
        verify(mockGameManager).setDiceRollS(6);
    }

    @Test
    void testProcessMovement_PlayerEntersRoom() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);
        when(mockGameManager.inRoom(testPlayer)).thenReturn(true);

        boolean result = turnService.processMovement(TEST_LOBBY_ID, TEST_PLAYER_NAME);

        assertTrue(result);
        assertEquals(TurnState.PLAYERS_TURN_SUGGEST, turnService.getTurnState(TEST_LOBBY_ID));
    }

    @Test
    void testProcessMovement_PlayerNotInRoom() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);
        when(mockGameManager.inRoom(testPlayer)).thenReturn(false);
        when(mockGameManager.checkGameEnd()).thenReturn(false);

        boolean result = turnService.processMovement(TEST_LOBBY_ID, TEST_PLAYER_NAME);

        assertTrue(result);
        assertEquals(TurnState.PLAYERS_TURN_ROLL_DICE, turnService.getTurnState(TEST_LOBBY_ID));
    }

    @Test
    void testProcessAccusation_Correct() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);
        turnService.initializeTurnState(TEST_LOBBY_ID);
        BasicCard roomCard = new BasicCard("Kitchen", null, CardType.ROOM);
        BasicCard weaponCard = new BasicCard("Candlestick", null, CardType.WEAPON);
        BasicCard suspectCard = new BasicCard("Colonel Mustard", null, CardType.CHARACTER);
        
        when(mockGameManager.getCardByName("Kitchen")).thenReturn(roomCard);
        when(mockGameManager.getCardByName("Candlestick")).thenReturn(weaponCard);
        when(mockGameManager.getCardByName("Colonel Mustard")).thenReturn(suspectCard);
        when(mockGameManager.makeAccusation(eq(testPlayer), any(SecretFile.class))).thenReturn(true);

        boolean result = turnService.processAccusation(TEST_LOBBY_ID, TEST_PLAYER_NAME, "Colonel Mustard", "Candlestick", "Kitchen");

        assertTrue(result);
        assertEquals(TurnState.PLAYER_HAS_WON, turnService.getTurnState(TEST_LOBBY_ID));
        verify(testPlayer).setHasWon(true);
        verify(mockGameManager).setState(GameState.ENDED);
    }

    @Test
    void testProcessAccusation_Incorrect() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);
        turnService.initializeTurnState(TEST_LOBBY_ID);
        BasicCard roomCard = new BasicCard("Kitchen", null, CardType.ROOM);
        BasicCard weaponCard = new BasicCard("Candlestick", null, CardType.WEAPON);
        BasicCard suspectCard = new BasicCard("Colonel Mustard", null, CardType.CHARACTER);
        
        when(mockGameManager.getCardByName("Kitchen")).thenReturn(roomCard);
        when(mockGameManager.getCardByName("Candlestick")).thenReturn(weaponCard);
        when(mockGameManager.getCardByName("Colonel Mustard")).thenReturn(suspectCard);
        when(mockGameManager.makeAccusation(eq(testPlayer), any(SecretFile.class))).thenReturn(false);
        when(mockGameManager.checkGameEnd()).thenReturn(false);

        boolean result = turnService.processAccusation(TEST_LOBBY_ID, TEST_PLAYER_NAME, "Colonel Mustard", "Candlestick", "Kitchen");

        assertTrue(result);
        verify(testPlayer).setActive(false);
    }

    @Test
    void testEndTurn_GameContinues() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.checkGameEnd()).thenReturn(false);

        turnService.endTurn(TEST_LOBBY_ID);

        assertEquals(TurnState.PLAYERS_TURN_ROLL_DICE, turnService.getTurnState(TEST_LOBBY_ID));
        verify(mockGameManager).nextTurn();
    }

    @Test
    void testEndTurn_GameEnds() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.checkGameEnd()).thenReturn(true);
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);

        turnService.endTurn(TEST_LOBBY_ID);

        assertEquals(TurnState.PLAYER_HAS_WON, turnService.getTurnState(TEST_LOBBY_ID));
        verify(mockGameManager, never()).nextTurn();
    }

    @Test
    void testNextTurn() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.checkGameEnd()).thenReturn(false);
        when(mockGameManager.getCurrentPlayerIndex()).thenReturn(1);
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);

        turnService.nextTurn(TEST_LOBBY_ID);

        assertEquals(TurnState.PLAYERS_TURN_ROLL_DICE, turnService.getTurnState(TEST_LOBBY_ID));
        verify(mockGameManager).nextTurn();
    }

    @Test
    void testSkipTurn() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.checkGameEnd()).thenReturn(false);
        when(mockGameManager.getCurrentPlayerIndex()).thenReturn(0);
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);

        turnService.skipTurn(TEST_LOBBY_ID, "Player disconnected");

        verify(mockGameManager).nextTurn();
    }

    @Test
    void testGetCurrentPlayer() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);
        Player result = turnService.getCurrentPlayer(TEST_LOBBY_ID);
        assertEquals(testPlayer, result);
    }

    @Test
    void testGetCurrentPlayer_NoGame() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(null);

        Player result = turnService.getCurrentPlayer(TEST_LOBBY_ID);
        assertNull(result);
    }

    @Test
    void testCanMakeSuggestion_Valid() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);
        turnService.initializeTurnState(TEST_LOBBY_ID);
        when(mockGameManager.inRoom(testPlayer)).thenReturn(true);
        turnService.processMovement(TEST_LOBBY_ID, TEST_PLAYER_NAME);

        boolean result = turnService.canMakeSuggestion(TEST_LOBBY_ID, TEST_PLAYER_NAME);
        assertTrue(result);
    }

    @Test
    void testCanMakeSuggestion_Invalid() {
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer2);

        boolean result = turnService.canMakeSuggestion(TEST_LOBBY_ID, TEST_PLAYER_NAME);
        assertFalse(result);
    }

    @Test
    void testCanMakeAccusation_Valid() {
        when(gameService.getGame(TEST_LOBBY_ID)).thenReturn(mockGameManager);
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer);
        turnService.initializeTurnState(TEST_LOBBY_ID);

        boolean result = turnService.canMakeAccusation(TEST_LOBBY_ID, TEST_PLAYER_NAME);
        assertTrue(result);
    }

    @Test
    void testCanMakeAccusation_Invalid() {
        when(mockGameManager.getCurrentPlayer()).thenReturn(testPlayer2);

        boolean result = turnService.canMakeAccusation(TEST_LOBBY_ID, TEST_PLAYER_NAME);
        assertFalse(result);
    }

}
