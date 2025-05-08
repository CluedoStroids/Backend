package at.aau.se2.cluedo.models;

import at.aau.se2.cluedo.models.gameboard.CellType;
import at.aau.se2.cluedo.models.gameboard.GameBoard;
import at.aau.se2.cluedo.models.gameboard.GameBoardCell;
import at.aau.se2.cluedo.models.gameobjects.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
        player = new Player("TestPlayer", "Miss Scarlet", 7, 24);
    }

    @Test
    @Disabled
    void testGameBoardInitialization() {
        // Check outer walls
        for (int x = 0; x < 25; x++) {
            assertEquals(CellType.WALL, gameBoard.getCell(x, 0).getCellType());
            assertEquals(CellType.WALL, gameBoard.getCell(x, 24).getCellType());
        }

        for (int y = 0; y < 25; y++) {
            assertEquals(CellType.WALL, gameBoard.getCell(0, y).getCellType());
            assertEquals(CellType.WALL, gameBoard.getCell(24, y).getCellType());
        }

        // Check a few room cells
        assertEquals(CellType.ROOM, gameBoard.getCell(3, 3).getCellType());
        assertNotNull(gameBoard.getCell(3, 3).getRoom());

        // Check a hallway cell
        assertEquals(CellType.HALLWAY, gameBoard.getCell(11, 11).getCellType());

        // Check a door
        assertEquals(CellType.DOOR, gameBoard.getCell(4, 6).getCellType());
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
        boolean moved = gameBoard.movePlayer(player, 7, 23);
        assertTrue(moved);
        assertEquals(7, player.getX());
        assertEquals(23, player.getY());
    }

    @Test
    void testMovePlayerToInvalidCell() {
        // Try moving to a wall
        boolean moved = gameBoard.movePlayer(player, 0, 0);
        assertFalse(moved);
        assertEquals(7, player.getX());
        assertEquals(24, player.getY());
    }

    @Test
    @Disabled
    void testMovePlayerThroughDoor() {
        // Position player near a door
        player.move(4, 7);

        // Move through the door
        boolean moved = gameBoard.movePlayer(player, 4, 6);
        assertTrue(moved);
        assertEquals(4, player.getX());
        assertEquals(6, player.getY());
    }

    @Test
    @Disabled
    void testUseSecretPassage() {
        // Position player at a secret passage
        player.move(5, 1);

        // Try to use the secret passage
        boolean used = gameBoard.useSecretPassage(player);
        assertTrue(used);

        // Player should have moved to the other end of the passage
        assertNotEquals(5, player.getX());
        assertNotEquals(1, player.getY());
    }

    @Test
    void testDisplayGameBoard() {
        // This is mostly a visual test, but we can at least ensure it doesn't throw
        List<Player> players = new ArrayList<>();
        players.add(player);

        gameBoard.displayGameBoard(players);
        // No assertion needed, just verify it runs without exceptions
    }
}
