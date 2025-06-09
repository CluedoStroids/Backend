package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.AccusationRequest;
import at.aau.se2.cluedo.dto.SuggestionRequest;
import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.cards.CardType;
import at.aau.se2.cluedo.models.gameboard.GameBoard;
import at.aau.se2.cluedo.models.gameboard.GameBoardCell;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import at.aau.se2.cluedo.services.GameService;
import at.aau.se2.cluedo.services.LobbyService;
import at.aau.se2.cluedo.services.TurnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class GameplayControllerTest {

    @Mock
    private LobbyService lobbyService;

    @Mock
    private GameService gameService;

    @Mock
    private GameManager gameManager;

    @Mock
    private GameBoard gameBoard;

    @Mock
    private TurnService turnService;

    @InjectMocks
    private GameplayController gameplayController;
    @InjectMocks
    private GameBoardController gameBoardController;

    private final String lobbyId = "test-lobby";
    private Player testPlayer;
    private SecretFile testSecretFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testPlayer = new Player("TestPlayer", "TestPlayer", 0, 0, PlayerColor.RED);
        testSecretFile = new SecretFile(new BasicCard("Colonel Mustard",new UUID(4,5), CardType.CHARACTER), new BasicCard("Knife",new UUID(4,5), CardType.WEAPON), new BasicCard("Kitchen",new UUID(4,5), CardType.ROOM));

        when(gameService.getGame(lobbyId)).thenReturn(gameManager);
        when(gameManager.getGameBoard()).thenReturn(gameBoard);
    }

    @Test
    void testMakeSuggestion() {
        // Arrange
        String suspectName = "Colonel Mustard";
        String weaponName = "Knife";
        String roomName = "Kitchen";
        SuggestionRequest request = new SuggestionRequest(testPlayer.getName(), suspectName, weaponName, roomName);

        // Mock turn service methods
        when(turnService.isPlayerTurn(lobbyId, testPlayer.getName())).thenReturn(true);
        when(turnService.canMakeSuggestion(lobbyId, testPlayer.getName())).thenReturn(true);
        when(turnService.processSuggestion(lobbyId, testPlayer.getName(), suspectName, weaponName)).thenReturn(true);

        // Act
        Map<String, Object> result = gameplayController.makeSuggestion(lobbyId, request);

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals(lobbyId, result.get("lobbyId"));
        assertEquals(testPlayer.getName(), result.get("player"));
        assertEquals(suspectName, result.get("suspect"));
        assertEquals(weaponName, result.get("weapon"));
        verify(turnService, times(1)).isPlayerTurn(lobbyId, testPlayer.getName());
        verify(turnService, times(1)).canMakeSuggestion(lobbyId, testPlayer.getName());
        verify(turnService, times(1)).processSuggestion(lobbyId, testPlayer.getName(), suspectName, weaponName);
    }

    @Test
    void testMakeAccusation() {
        // Arrange
        String suspectName = "Colonel Mustard";
        String weaponName = "Knife";
        String roomName = "Kitchen";
        AccusationRequest request = new AccusationRequest(testPlayer.getName(), suspectName, weaponName, roomName);

        // Mock turn service methods
        when(turnService.isPlayerTurn(lobbyId, testPlayer.getName())).thenReturn(true);
        when(turnService.canMakeAccusation(lobbyId, testPlayer.getName())).thenReturn(true);
        when(turnService.processAccusation(lobbyId, testPlayer.getName(), suspectName, weaponName, roomName)).thenReturn(true);

        // Act
        Map<String, Object> result = gameplayController.makeAccusation(lobbyId, request);

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals(lobbyId, result.get("lobbyId"));
        assertEquals(testPlayer.getName(), result.get("player"));
        assertEquals(suspectName, result.get("suspect"));
        assertEquals(weaponName, result.get("weapon"));
        assertEquals(roomName, result.get("room"));
        verify(turnService, times(1)).isPlayerTurn(lobbyId, testPlayer.getName());
        verify(turnService, times(1)).canMakeAccusation(lobbyId, testPlayer.getName());
        verify(turnService, times(1)).processAccusation(lobbyId, testPlayer.getName(), suspectName, weaponName, roomName);
    }

    @Test
    void testDisplayGameBoard() {
        // Arrange
        List<Player> players = new ArrayList<>();
        players.add(testPlayer);

        // Act
        String result = gameplayController.displayGameBoard(lobbyId, players);

        // Assert
        assertEquals(lobbyId, result);
        verify(gameManager.getGameBoard(), times(1)).displayGameBoard(players);
    }

    @Test
    void testGetGameBoard() {
        setUp();
        // Act
        GameBoardCell[][] result = gameBoardController.getGameBoard(lobbyId);

        // Assert
        assertSame(gameBoard.getGrid(), result);
        verify(gameManager, times(1)).getGameBoard().getGrid();
    }
}
