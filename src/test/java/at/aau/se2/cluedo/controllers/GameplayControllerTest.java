package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.cards.CardType;
import at.aau.se2.cluedo.models.gameboard.GameBoard;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import at.aau.se2.cluedo.services.GameService;
import at.aau.se2.cluedo.services.LobbyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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

    @InjectMocks
    private GameplayController gameplayController;

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

        // Act
        String result = gameplayController.makeSuggestion(lobbyId, testPlayer, suspectName, weaponName);

        // Assert
        assertEquals(lobbyId, result);
        verify(gameManager, times(1)).makeSuggestion(testPlayer, suspectName, weaponName);
    }

    @Test
    void testMakeAccusation() {
        // Arrange
        when(gameService.makeAccusation(testPlayer, testSecretFile,lobbyId)).thenReturn("Wrong! testPlayer is out of the game!");

        // Act
        String result = gameplayController.makeAccusation(lobbyId, testPlayer, testSecretFile);

        // Assert
        assertEquals("Wrong! testPlayer is out of the game!", result);
        verify(gameService, times(1)).makeAccusation(testPlayer, testSecretFile,lobbyId);
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
        // Act
        GameBoard result = gameplayController.getGameBoard(lobbyId);

        // Assert
        assertSame(gameBoard, result);
        verify(gameManager, times(1)).getGameBoard();
    }
}
