package at.aau.se2.cluedo.models.gameboard;



import at.aau.se2.cluedo.models.gameobjects.Player;
import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameBoard {
    private static final Logger logger = LoggerFactory.getLogger(GameBoard.class);

    // ANSI color codes
    public static final String RED = "\u001B[41m";
    public static final String YELLOW = "\u001B[43m";
    public static final String WHITE = "\u001B[47m";
    public static final String GREEN = "\u001B[42m";
    public static final String BLUE = "\u001B[44m";
    public static final String PURPLE = "\u001B[45m";
    public static final String RESET = "\u001B[0m";
    public static final int WIDTH = 25;
    public static final int HEIGHT = 25;
    @Getter
    private final GameBoardCell[][] grid;
    private final Map<String, Room> rooms;
    private final Map<Room, Room> secretPassages;

    public GameBoard() {
        this.grid = new GameBoardCell[WIDTH][HEIGHT];
        this.rooms = new HashMap<>();
        this.secretPassages = new HashMap<>();
        initializeGameBoard();
        createRooms();
        initializeSecretPassages();
    }


    private void initializeGameBoard() {
        // 1. Initialize everything as hallways
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                grid[x][y] = new GameBoardCell(x, y, CellType.HALLWAY);
            }
        }

        // 2. Set outer border as walls
        for (int x = 0; x < WIDTH; x++) {
            grid[x][0].setCellType(CellType.WALL);
            grid[x][HEIGHT - 1].setCellType(CellType.WALL);
        }
        for (int y = 0; y < HEIGHT; y++) {
            grid[0][y].setCellType(CellType.WALL);
            grid[WIDTH - 1][y].setCellType(CellType.WALL);
        }

        // 3. Set central block as walls (5x5 in center)
        for (int x = WIDTH / 2 - 2; x <= WIDTH / 2 + 2; x++) {
            for (int y = HEIGHT / 2 - 2; y <= HEIGHT / 2 + 2 + 2; y++) {
                grid[x][y].setCellType(CellType.WALL);
            }
        }
    }

    private void createRooms() {
        defineRooms();
        defineDoors();
        defineHallways();
    }

    private void defineRooms() {
        // Definition of all 9 rooms: [Name, x, y, width, height]
        String[][] roomDefinitions = {
                {"Kitchen", "0", "1", "6", "6"},
                {"Ballroom", "8", "1", "8", "7"},
                {"Conservatory", "18", "1", "6", "5"},
                {"Dining Room", "0", "9", "8", "7"},
                {"Billiard Room", "18", "8", "7", "5"},
                {"Library", "17", "14", "8", "5"},
                {"Lounge", "0", "19", "7", "6"},
                {"Hall", "9", "18", "6", "7"},
                {"Study", "17", "21", "8", "4"},
        };

        for (String[] def : roomDefinitions) {
            createRoom(def[0],
                    Integer.parseInt(def[1]),
                    Integer.parseInt(def[2]),
                    Integer.parseInt(def[3]),
                    Integer.parseInt(def[4]));
        }
    }

    private void defineDoors() {
        int[][] doorPositions = {
                {4, 6}, {8, 5}, {9, 7}, {14, 7}, {15, 5}, {18, 5},
                {7, 12}, {6, 15}, {18, 9}, {23, 12}, {21, 14},
                {17, 16}, {5, 19}, {11, 18}, {12, 18}, {14, 20},
                {17, 21}, {6, 24}
        };

        for (int[] pos : doorPositions) {
            setDoor(pos[0], pos[1]);
        }
    }

    private void defineHallways() {
        int[][] hallwayPositions = {
                {9, 1}, {8, 1}, {14, 1}, {15, 1},
                {6, 9}, {7, 9}, {17, 14}, {17, 18},
                {7, 24}, {0, 17}, {9, 0}, {14, 0},
                {24, 6}, {24, 19}
        };

        for (int[] pos : hallwayPositions) {
            setHallway(pos[0], pos[1]);
        }
    }


    private void createRoom(String name, int startX, int startY, int width, int height) {
        Room room = new Room(name);
        rooms.put(name, room);

        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                if (x < WIDTH && y < HEIGHT) {
                    grid[x][y].setCellType(CellType.ROOM);
                    grid[x][y].setRoom(room);
                }
            }
        }
    }

    private void setDoor(int x, int y) {
        grid[x][y].setCellType(CellType.DOOR);
    }

    private void setHallway(int x, int y) {
        grid[x][y].setCellType(CellType.HALLWAY);
    }

    private void initializeSecretPassages() {
        // Secret passages as array with [Room1, Room2]
        String[][] secretPassageDefinitions = {
                {"Kitchen", "Study"},
                {"Lounge", "Conservatory"}
        };

        for (String[] passage : secretPassageDefinitions) {
            secretPassages.put(rooms.get(passage[0]), rooms.get(passage[1]));
            secretPassages.put(rooms.get(passage[1]), rooms.get(passage[0]));
        }

        // Mark secret passage cells
        grid[5][1].setCellType(CellType.SECRET_PASSAGE);    // Kitchen
        grid[23][5].setCellType(CellType.SECRET_PASSAGE);   // Conservatory
        //grid[0][19].setCellType(CellType.SECRET_PASSAGE);   // Lounge

        grid[4][24].setCellType(CellType.SECRET_PASSAGE);   // for Test cases
        grid[24][21].setCellType(CellType.SECRET_PASSAGE);   // Study
    }

    public GameBoardCell getCell(int x, int y) {
        if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
            return grid[x][y];
        }
        return null;
    }

    public boolean movePlayer(Player player, int newX, int newY, boolean teleport) {
        GameBoardCell target = getCell(newX, newY);
        GameBoardCell current = getCell(player.getX(), player.getY());

        if (!isMoveValid(current, target, teleport)) return false;

        if (target.getCellType() == CellType.DOOR) {
            int[] corrected = adjustPositionForDoor(current, target);
            newX = corrected[0];
            newY = corrected[1];
        }

        player.move(newX, newY);
        updateRoomPresence(current, target, player);

        if (target.getCellType() == CellType.SECRET_PASSAGE) {
            return useSecretPassage(player);
        }

        return true;
    }

    private boolean isMoveValid(GameBoardCell from, GameBoardCell to, boolean teleport) {
        return to != null && to.isAccessible() && (
                teleport ||
                from.getCellType() == to.getCellType() ||
                from.getCellType() == CellType.DOOR ||
                to.getCellType() == CellType.DOOR ||
                from.getCellType() == CellType.SECRET_PASSAGE ||
                to.getCellType() == CellType.SECRET_PASSAGE
        );
    }

    private int[] adjustPositionForDoor(GameBoardCell from, GameBoardCell door) {
        int[][] directions = {{0,1}, {0,-1}, {1,0}, {-1,0}};
        for (int[] dir : directions) {
            GameBoardCell neighbor = getCell(door.getX() + dir[0], door.getY() + dir[1]);
            if (neighbor != null && neighbor.getCellType() != CellType.DOOR &&
                    from.getCellType() != neighbor.getCellType()) {
                return new int[]{door.getX() + dir[0], door.getY() + dir[1]};
            }
        }
        return new int[]{from.getX(), from.getY()};
    }

    private void updateRoomPresence(GameBoardCell from, GameBoardCell to, Player player) {
        if (from.getCellType() == CellType.ROOM) from.getRoom().playerLeavesRoom(player);
        if (to.getCellType() == CellType.ROOM) to.getRoom().playerEntersRoom(player);
    }

    public boolean useSecretPassage(Player player) {
        GameBoardCell current = getCell(player.getX(), player.getY());
        if (current.getCellType() != CellType.SECRET_PASSAGE || current.getRoom() == null) return false;

        Room target = secretPassages.get(current.getRoom());
        if (target == null) return false;

        GameBoardCell targetCell = findPassageExitInRoom(target);
        if (targetCell != null) {
            player.move(targetCell.getX(), targetCell.getY());
            return true;
        }
        return false;
    }

    private GameBoardCell findPassageExitInRoom(Room room) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                GameBoardCell cell = grid[x][y];
                if (isSecretPassageInRoom(cell, room)) {
                    GameBoardCell exit = findAccessibleNeighbor(x, y);
                    if (exit != null) {
                        return exit;
                    }
                }
            }
        }
        return null;
    }

    private boolean isSecretPassageInRoom(GameBoardCell cell, Room room) {
        return cell.getRoom() == room && cell.getCellType() == CellType.SECRET_PASSAGE;
    }

    private GameBoardCell findAccessibleNeighbor(int x, int y) {
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] d : directions) {
            GameBoardCell neighbor = getCell(x + d[0], y + d[1]);
            if (neighbor != null && neighbor.isAccessible()) {
                return neighbor;
            }
        }
        return null;
    }
    public void teleportPlayerToRoom(Player player, Room room){
        List<GameBoardCell> roomCells = new ArrayList<>();
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                GameBoardCell cell = grid[x][y];
                if(cell.getRoom() == room && cell.getCellType() == CellType.ROOM){
                    roomCells.add(cell);
                }
            }
        }

        String time = String.valueOf(System.currentTimeMillis());
        int lastDigitsCount = 7;
        try {
            GameBoardCell targetCell = roomCells.get(Integer.parseInt(time.substring(time.length()-lastDigitsCount)) % roomCells.size());
            movePlayer(player, targetCell.getX(),targetCell.getY(),true);
        } catch (Exception e) {
            teleportPlayerToRoom(player,room);
        }
    }

    public void displayGameBoard(List<Player> players) {
        logger.debug("\n=== CLUE GAME BOARD ===");


        logger.debug("  ");
        for (int i = 0; i < WIDTH; i++) {
            logger.debug("{} ", i % 10);

        }

        for (int y = 0; y < HEIGHT; y++) {
            logger.debug("{} ", y % 10);

            for (int x = 0; x < WIDTH; x++) {

                GameBoardCell cell = grid[x][y];
                char symbol = getSymbol(cell);
                String color = getColor(players, x, y);

                logger.debug("{}{}" + RESET + " ", color, symbol);
            }
        }
    }

    private char getSymbol(GameBoardCell cell) {
        char retChar;
        switch (cell.getCellType()) {
            case HALLWAY -> retChar = '.';
            case ROOM -> retChar = cell.getRoom().getName().charAt(0);
            case WALL -> retChar = '|';
            case SECRET_PASSAGE -> retChar = 'G';
            case DOOR -> retChar = 'D';
            default -> retChar = '?';

        }

        return retChar;
    }

    public String getColor(List<Player> players, int x, int y) {
        for (Player p : players) {
            if (p.getX() == x && p.getY() == y) {
                return switch (p.getColor()) {
                    case RED -> RED;
                    case YELLOW -> YELLOW;
                    case WHITE -> WHITE;
                    case GREEN -> GREEN;
                    case BLUE -> BLUE;
                    case PURPLE -> PURPLE;
                };
            }
        }
        return "";
    }
}