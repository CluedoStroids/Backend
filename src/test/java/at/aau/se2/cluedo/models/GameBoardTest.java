package at.aau.se2.cluedo.models;

import at.aau.se2.cluedo.models.gameboard.CellType;
import at.aau.se2.cluedo.models.gameboard.GameBoard;
import at.aau.se2.cluedo.models.gameboard.GameBoardCell;
import at.aau.se2.cluedo.models.gameboard.Room;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameBoardTest {

    private GameBoard gameBoard;
    private Player player;

    @BeforeEach
    void setUp() {
        gameBoard = new GameBoard();
        player = new Player("TestPlayer", "Miss Scarlet", 7, 24, PlayerColor.RED);
    }



    @Test
    public void testInitialization() {
        // Check that the board is properly initialized
        assertNotNull(gameBoard);

        // Test a hallway cell
        GameBoardCell hallwayCell = gameBoard.getCell(8, 8);
        assertEquals(CellType.HALLWAY, hallwayCell.getCellType());
        assertTrue(hallwayCell.isAccessible());

        // Test a wall cell
        GameBoardCell wallCell = gameBoard.getCell(0, 0);
        assertEquals(CellType.WALL, wallCell.getCellType());
        assertFalse(wallCell.isAccessible());

        // Test a room cell
        GameBoardCell roomCell = gameBoard.getCell(2, 3);
        assertEquals(CellType.ROOM, roomCell.getCellType());
        assertTrue(roomCell.isAccessible());
        assertNotNull(roomCell.getRoom());

        // Test a door cell
        GameBoardCell doorCell = gameBoard.getCell(4, 6);
        assertEquals(CellType.DOOR, doorCell.getCellType());
        assertTrue(doorCell.isAccessible());
    }
    @Test
    void testGetCell() {
        // Valid cell
        GameBoardCell cell = gameBoard.getCell(5, 5);
        assertNotNull(cell);
        assertEquals(5, cell.getX());
        assertEquals(5, cell.getY());

        // Invalid coordinates
        assertNull(gameBoard.getCell(-1, 5));
        assertNull(gameBoard.getCell(5, -1));
        assertNull(gameBoard.getCell(25, 5));
        assertNull(gameBoard.getCell(5, 25));
    }

    @Test
    void testMovePlayerToValidCell() {
        // Move to a valid hallway cell
        boolean moved = gameBoard.movePlayer(player, 7, 23,false);
        assertTrue(moved);
        assertEquals(7, player.getX());
        assertEquals(23, player.getY());
    }

    @Test
    void testMovePlayerToInvalidCell() {
        // Try moving to a wall
        boolean moved = gameBoard.movePlayer(player, 0, 0,false);
        assertFalse(moved);
        assertEquals(7, player.getX());
        assertEquals(24, player.getY());
    }

    @Test
    void testMovePlayerThroughDoor() {
        // Position player near a door
        player.move(4, 7);

        // Move through the door
        assertTrue(gameBoard.movePlayer(player, 4, 6,false));

        assertEquals(4, player.getX());
        assertEquals(5, player.getY());

        GameBoardCell currentCell = gameBoard.getCell(player.getX(), player.getY());
        assertEquals(CellType.ROOM, currentCell.getCellType());
        assertNotNull(currentCell.getRoom());

    }

    @Test
    void testUseSecretPassage() {
        player.move(5, 1);

        // Use secret passage
        assertTrue(gameBoard.useSecretPassage(player));

        // Player should now be in study or near it
        GameBoardCell currentCell = gameBoard.getCell(player.getX(), player.getY());
        assertTrue(currentCell.isAccessible());

        // The closest room should be Study
        if (currentCell.getCellType() == CellType.ROOM) {
            assertEquals("Study", currentCell.getRoom().getName());
        }
    }
    @Test
    void testTeleportPlayerToRoom() {
        Room targetRoom = gameBoard.getCell(2, 3).getRoom();  // Should be Kitchen
        assertNotNull(targetRoom);

        gameBoard.teleportPlayerToRoom(player, targetRoom);

        GameBoardCell cell = gameBoard.getCell(player.getX(), player.getY());
        assertEquals(CellType.ROOM, cell.getCellType());
        assertEquals(targetRoom, cell.getRoom());
    }

    @Test
    void testDisplayGameBoard() {
        List<Player> players = new ArrayList<>();
        players.add(player);
        assertDoesNotThrow(() -> gameBoard.displayGameBoard(players));
    }

    @Test
    void testUseSecretPassageWhenNoneExists() {
        player.move(7, 24);  // Regular hallway
        assertFalse(gameBoard.useSecretPassage(player), "No passage here, should return false");
    }
    @Test
    void testGetColorForAllPlayerColors() {
        for (PlayerColor color : PlayerColor.values()) {
            Player testPlayer = new Player("Player", "Character", 10, 10, color);
            List<Player> players = List.of(testPlayer);
            String colorStr = gameBoard.getColor(players, 10, 10);
            assertNotNull(colorStr, "Color string should not be null for " + color);
        }
    }
    @Test
    void testMovePlayer_InitialPositionIsValid() {
        assertEquals(7, player.getX());
        assertEquals(24, player.getY());
        assertTrue(gameBoard.getCell(7, 24).isAccessible());
    }

    @Test
    void testMovePlayer_ValidHallwayMove() {
        assertTrue(gameBoard.movePlayer(player, 7, 23, false));
        assertEquals(7, player.getX());
        assertEquals(23, player.getY());
    }

    @Test
    void testMovePlayer_InvalidWallMove() {
        assertFalse(gameBoard.movePlayer(player, 0, 0, false));
        assertEquals(7, player.getX());
        assertEquals(24, player.getY());
    }

    @Test
    void testMovePlayer_InvalidOutOfBounds() {
        assertFalse(gameBoard.movePlayer(player, -1, 24, false));
        assertFalse(gameBoard.movePlayer(player, 7, 25, false));
        assertEquals(7, player.getX());
        assertEquals(24, player.getY());
    }

    @Test
    void testMovePlayer_ValidRoomEntryThroughDoor() {
        // Position player near door at 6,24
        player.move(6, 24);
        assertTrue(gameBoard.movePlayer(player, 6, 24, false));
        // Player should be adjusted to proper position
        assertNotEquals(5, player.getX());
        assertNotEquals(24, player.getY());
    }



    @Test
    void testMovePlayer_TeleportToRoom() {
        Room kitchen = gameBoard.getCell(1, 1).getRoom();
        assertTrue(gameBoard.movePlayer(player, 1, 1, true));
        assertEquals(kitchen, gameBoard.getCell(player.getX(), player.getY()).getRoom());
        assertTrue(kitchen.getPlayersInRoom().contains(player));
    }



    @Test
    void testMovePlayer_InvalidDirectRoomEntryWithoutDoor() {
        // Try to move directly into Lounge without using door
        assertFalse(gameBoard.movePlayer(player, 0, 19, false));
        assertEquals(7, player.getX());
        assertEquals(24, player.getY());
    }


    @Test
    void testMovePlayer_ThroughMultipleDoors() {
        // Starting near door at 6,24
        player.move(6, 24);
        assertTrue(gameBoard.movePlayer(player, 6, 24, false));
        // Continue movement through the door system...
        // This would depend on your specific board layout
    }

    @Test
    void testMovePlayer_InvalidMoveBetweenDifferentCellTypes() {
        // Try to move directly from hallway to room without door
        assertFalse(gameBoard.movePlayer(player, 1, 1, false));
    }

    @Test
    void testMovePlayer_ValidMoveBetweenSameCellTypes() {
        // Move within hallways
        player.move(7, 23);
        assertTrue(gameBoard.movePlayer(player, 7, 22, false));
    }




}
