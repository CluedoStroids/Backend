package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import at.aau.se2.cluedo.services.LobbyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GameplayControllerTest {

    @Mock
    private LobbyService lobbyService;

    @InjectMocks
    private GameplayController gameplayController;

    private Player testPlayer;
    private SecretFile testSecretFile;
    private List<String> testMovement;
    private List<Player> testPlayers;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testPlayer = new Player("TestPlayer", "TestPlayer", 0, 0, PlayerColor.RED);
        testSecretFile = mock(SecretFile.class);
        testMovement = Arrays.asList("W", "A", "S", "D");
        testPlayers = Arrays.asList(
            new Player("Player1", "Player1", 0, 0, PlayerColor.RED),
            new Player("Player2", "Player2", 0, 0, PlayerColor.BLUE)
        );
    }

    @Test
    void makeSuggestion_ShouldCallLobbyService() {
        // Arrange
        String suspect = "Colonel Mustard";
        String weapon = "Candlestick";
        String expectedResult = "No one could disprove your suggestion!";
        when(lobbyService.makeSuggestion(testPlayer, suspect, weapon)).thenReturn(expectedResult);

        // Act
        String result = gameplayController.makeSuggestion(testPlayer, suspect, weapon);

        // Assert
        assertEquals(expectedResult, result);
        verify(lobbyService).makeSuggestion(testPlayer, suspect, weapon);
    }

    @Test
    void makeAccusation_ShouldCallLobbyService() {
        // Arrange
        String expectedResult = "Wrong! TestPlayer is out of the game!";
        when(lobbyService.makeAccusation(testPlayer, testSecretFile)).thenReturn(expectedResult);

        // Act
        String result = gameplayController.makeAccusation(testPlayer, testSecretFile);

        // Assert
        assertEquals(expectedResult, result);
        verify(lobbyService).makeAccusation(testPlayer, testSecretFile);
    }

    /*@Test
    void performMovement_ShouldCallLobbyService() {
        // Arrange
        int expectedResult = 0;
        when(lobbyService.performMovement(testPlayer, testMovement)).thenReturn(expectedResult);

        // Act
        int result = gameplayController.performMovement(testPlayer, testMovement);

        // Assert
        assertEquals(expectedResult, result);
        verify(lobbyService).performMovement(testPlayer, testMovement);
    }*/

    @Test
    void displayGameBoard_ShouldCallLobbyService() {
        // Arrange
        String expectedResult = "[0,0,0,1,0,1,0";
        when(lobbyService.displayGameBoard(testPlayers)).thenReturn(expectedResult);

        // Act
        String result = gameplayController.displayGameBoard(testPlayers);

        // Assert
        assertEquals(expectedResult, result);
        verify(lobbyService).displayGameBoard(testPlayers);
    }
}
