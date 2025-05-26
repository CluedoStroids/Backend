package at.aau.se2.cluedo.dto;

import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import at.aau.se2.cluedo.models.lobby.Lobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// fixme dont need to Lombok gen capabilities
class LobbyResponseTest {

    private Lobby lobby;
    private Player host;
    private Player player2;
    private Player player3;
    private List<Player> players;

    @BeforeEach
    void setUp() {
        // Create test players
        host = new Player("HostPlayer", "Colonel Mustard", 0, 0, PlayerColor.RED);
        player2 = new Player("Player2", "Miss Scarlet", 1, 1, PlayerColor.GREEN);
        player3 = new Player("Player3", "Professor Plum", 2, 2, PlayerColor.BLUE);

        // Create players list
        players = new ArrayList<>(Arrays.asList(host, player2, player3));

        // Create a test lobby with the host
        lobby = new Lobby("test-lobby-id", host);

        // Add additional players to the lobby
        lobby.addPlayer(player2);
        lobby.addPlayer(player3);
    }

    @Test
    void testFromLobby() {
        // Convert lobby to LobbyResponse using the fromLobby static method
        LobbyResponse response = LobbyResponse.fromLobby(lobby);

        // Assert that the response has the correct data
        assertEquals(lobby.getId(), response.getId());
        assertEquals(host, response.getHost());
        assertEquals(3, response.getPlayers().size());
        assertTrue(response.getPlayers().contains(host));
        assertTrue(response.getPlayers().contains(player2));
        assertTrue(response.getPlayers().contains(player3));
    }

    @Test
    void testConstructor() {
        // Create a LobbyResponse using the constructor
        LobbyResponse response = new LobbyResponse("test-id", host, players);

        // Assert constructor assigned fields correctly
        assertEquals("test-id", response.getId());
        assertEquals(host, response.getHost());
        assertEquals(players, response.getPlayers());
    }

    @Test
    void testNoArgsConstructor() {
        // Create a LobbyResponse using the no-args constructor
        LobbyResponse response = new LobbyResponse();

        // Assert that fields are null or default values
        assertNull(response.getId());
        assertNull(response.getHost());
        assertNull(response.getPlayers());
    }

    @Test
    void testGettersAndSetters() {
        // Create a LobbyResponse
        LobbyResponse response = new LobbyResponse();

        // Set values using setters
        response.setId("new-id");
        response.setHost(host);
        response.setPlayers(players);

        // Assert getters return the correct values
        assertEquals("new-id", response.getId());
        assertEquals(host, response.getHost());
        assertEquals(players, response.getPlayers());
    }

    @Test
    void testEqualsAndHashCode() {
        // Create two identical LobbyResponse objects
        LobbyResponse response1 = new LobbyResponse("test-id", host, players);
        LobbyResponse response2 = new LobbyResponse("test-id", host, players);

        // Create a different LobbyResponse
        LobbyResponse differentResponse = new LobbyResponse("different-id", host, players);

        // Test equals and hashCode
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1, differentResponse);
        assertNotEquals(response1.hashCode(), differentResponse.hashCode());
    }

    @Test
    void testToString() {
        // Create a LobbyResponse
        LobbyResponse response = new LobbyResponse("test-id", host, players);

        // Get the toString representation
        String toString = response.toString();

        // Assert that toString contains important field information
        assertTrue(toString.contains("test-id"));
    }
}
