package at.aau.se2.cluedo.models;

import at.aau.se2.cluedo.models.gameboard.Room;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoomTest {

    private Room room;
    private Player player;

    @BeforeEach
    void setUp() {
        room = new Room("Kitchen");
        player = new Player("TestPlayer", "Miss Scarlet", 0, 0, PlayerColor.RED);
    }

    @Test
    void testRoomCreation() {
        assertEquals("Kitchen", room.getName());
        assertTrue(room.getPlayersInRoom().isEmpty());
    }

    @Test
    void testPlayerEntersRoom() {
        room.playerEntersRoom(player);

        List<Player> playersInRoom = room.getPlayersInRoom();
        assertEquals(1, playersInRoom.size());
        assertTrue(playersInRoom.contains(player));
    }

    @Test
    void testPlayerLeavesRoom() {
        room.playerEntersRoom(player);
        room.playerLeavesRoom(player);

        List<Player> playersInRoom = room.getPlayersInRoom();
        assertTrue(playersInRoom.isEmpty());
    }

    @Test
    void testGetPlayersInRoomReturnsDefensiveCopy() {
        room.playerEntersRoom(player);

        List<Player> playersInRoom = room.getPlayersInRoom();
        playersInRoom.clear(); // This should not affect the original list

        assertEquals(1, room.getPlayersInRoom().size());
    }
}
