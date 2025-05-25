package at.aau.se2.cluedo.models;

import at.aau.se2.cluedo.models.gameboard.CellType;
import at.aau.se2.cluedo.models.gameboard.GameBoardCell;
import at.aau.se2.cluedo.models.gameboard.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameBoardCellTest {

    private GameBoardCell hallwayCell;
    private GameBoardCell wallCell;
    private GameBoardCell roomCell;
    private Room room;

    @BeforeEach
    void setUp() {
        hallwayCell = new GameBoardCell(1, 2, CellType.HALLWAY);
        wallCell = new GameBoardCell(3, 4, CellType.WALL);
        roomCell = new GameBoardCell(5, 6, CellType.ROOM);
        room = new Room("TestRoom");
        roomCell.setRoom(room);
    }

    @Test
    void testCellCreation() {
        assertEquals(1, hallwayCell.getX());
        assertEquals(2, hallwayCell.getY());
        assertEquals(CellType.HALLWAY, hallwayCell.getCellType());

        assertEquals(3, wallCell.getX());
        assertEquals(4, wallCell.getY());
        assertEquals(CellType.WALL, wallCell.getCellType());
    }

    @Test
    void testIsAccessible() {
        assertTrue(hallwayCell.isAccessible());
        assertFalse(wallCell.isAccessible());
        assertTrue(roomCell.isAccessible());
    }

    @Test
    void testSetCellType() {
        hallwayCell.setCellType(CellType.DOOR);
        assertEquals(CellType.DOOR, hallwayCell.getCellType());
    }

    @Test
    void testRoomAssociation() {
        assertNull(hallwayCell.getRoom());

        assertEquals(room, roomCell.getRoom());
        assertEquals("TestRoom", roomCell.getRoom().getName());

        // Change the room
        Room newRoom = new Room("NewRoom");
        roomCell.setRoom(newRoom);
        assertEquals(newRoom, roomCell.getRoom());
        assertEquals("NewRoom", roomCell.getRoom().getName());
    }
}
