package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.ActiveLobbiesResponse;
import at.aau.se2.cluedo.dto.GetActiveLobbiesRequest;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import at.aau.se2.cluedo.models.lobby.Lobby;
import at.aau.se2.cluedo.services.LobbyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LobbyControllerTest {

    @Mock
    private LobbyService lobbyService;

    @InjectMocks
    private LobbyController lobbyController;

    private Player player1;
    private Player player2;
    private Lobby lobby1;
    private Lobby lobby2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        player1 = new Player("Player1", "Player1", 0, 0, PlayerColor.RED);
        player2 = new Player("Player2", "Player2", 0, 0, PlayerColor.BLUE);

        lobby1 = new Lobby("lobby-id-1", player1);
        lobby2 = new Lobby("lobby-id-2", player2);
    }

    @Test
    void getActiveLobbies_ShouldReturnAllActiveLobbies() {
        // Arrange
        List<Lobby> lobbies = Arrays.asList(lobby1, lobby2);
        when(lobbyService.getAllActiveLobbies()).thenReturn(lobbies);

        // Act
        ActiveLobbiesResponse response = lobbyController.getActiveLobbies(new GetActiveLobbiesRequest());

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getLobbies().size());

        assertEquals("lobby-id-1", response.getLobbies().get(0).getId());
        assertEquals("Player1", response.getLobbies().get(0).getHostName());
        assertEquals(1, response.getLobbies().get(0).getPlayerCount());

        assertEquals("lobby-id-2", response.getLobbies().get(1).getId());
        assertEquals("Player2", response.getLobbies().get(1).getHostName());
        assertEquals(1, response.getLobbies().get(1).getPlayerCount());

        verify(lobbyService, times(1)).getAllActiveLobbies();
    }
}
