package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.models.cards.BasicCard;
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


}
