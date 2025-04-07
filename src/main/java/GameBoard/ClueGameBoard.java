package GameBoard;

import at.aau.serg.websocketdemoserver.GameObjects.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClueGameBoard {
    private static final int WIDTH = 25;
    private static final int HEIGHT = 25;
    private final GameBoardCell[][] grid;
    private final Map<String, Room> rooms;
    private final Map<Room, Room> secretPassages;

    // ANSI color codes
    public static final String RED = "\u001B[31m";
    public static final String YELLOW = "\u001B[33m";
    public static final String WHITE = "\u001B[37m";
    public static final String GREEN = "\u001B[32m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String RESET = "\u001B[0m";

    public ClueGameBoard() {
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
                grid[x][y] = new GameBoardCell(x, y, GameBoardCell.CellType.HALLWAY);
            }
        }

        // 2. Set outer border as walls
        for (int x = 0; x < WIDTH; x++) {
            grid[x][0].setCellType(GameBoardCell.CellType.WALL);
            grid[x][HEIGHT-1].setCellType(GameBoardCell.CellType.WALL);
        }
        for (int y = 0; y < HEIGHT; y++) {
            grid[0][y].setCellType(GameBoardCell.CellType.WALL);
            grid[WIDTH-1][y].setCellType(GameBoardCell.CellType.WALL);
        }

        // 3. Set central block as walls (5x5 in center)
        for (int x = WIDTH/2 - 2; x <= WIDTH/2 + 2; x++) {
            for (int y = HEIGHT/2 - 2; y <= HEIGHT/2+2 + 2; y++) {
                grid[x][y].setCellType(GameBoardCell.CellType.WALL);
            }
        }
    }

    private void createRooms() {
        // Definition of all 9 rooms as array with [Name, x, y, width, height]
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

        // Create rooms
        for (String[] roomDef : roomDefinitions) {
            createRoom(roomDef[0],
                    Integer.parseInt(roomDef[1]),
                    Integer.parseInt(roomDef[2]),
                    Integer.parseInt(roomDef[3]),
                    Integer.parseInt(roomDef[4]));
        }

        // Position doors
        setDoor(4, 6);
        setDoor(8, 5);
        setDoor(9, 7);
        setDoor(14, 7);
        setDoor(15, 5);
        setDoor(18, 5);
        setDoor(7, 12);
        setDoor(6, 15);
        setDoor(18, 9);
        setDoor(23, 12);
        setDoor(21, 14);

        setDoor(18, 16);
        setDoor(5, 19);
        setDoor(11, 18);
        setDoor(12, 18);
        setDoor(14, 20);
        setDoor(17, 21);
    }

    private void createRoom(String name, int startX, int startY, int width, int height) {
        Room room = new Room(name);
        rooms.put(name, room);

        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                if (x < WIDTH && y < HEIGHT) {
                    grid[x][y].setCellType(GameBoardCell.CellType.ROOM);
                    grid[x][y].setRoom(room);
                }
            }
        }
    }

    private void setDoor(int x, int y) {
        grid[x][y].setCellType(GameBoardCell.CellType.DOOR);
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
        grid[5][1].setCellType(GameBoardCell.CellType.SECRET_PASSAGE);    // Kitchen
        grid[23][5].setCellType(GameBoardCell.CellType.SECRET_PASSAGE);   // Conservatory
        grid[0][19].setCellType(GameBoardCell.CellType.SECRET_PASSAGE);   // Lounge
        grid[24][21].setCellType(GameBoardCell.CellType.SECRET_PASSAGE);   // Study
    }

    public GameBoardCell getCell(int x, int y) {
        if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
            return grid[x][y];
        }
        return null;
    }

    public boolean movePlayer(Player player, int newX, int newY) {
        GameBoardCell targetCell = getCell(newX, newY);
        if (targetCell == null || !targetCell.isAccessible() ) {
            return false;
        }

        // Clear current position
        GameBoardCell currentCell = getCell(player.getX(), player.getY());

        // Set new position
        player.move(newX, newY);

        // Room logic
        if (currentCell.getCellType() == GameBoardCell.CellType.ROOM) {
            currentCell.getRoom().playerLeavesRoom(player);
        }

        if (targetCell.getCellType() == GameBoardCell.CellType.ROOM) {
            targetCell.getRoom().playerEntersRoom(player);
        }

        return true;
    }

    public boolean useSecretPassage(Player player) {
        GameBoardCell currentCell = getCell(player.getX(), player.getY());
        if (currentCell.getCellType() != GameBoardCell.CellType.SECRET_PASSAGE) {
            return false;
        }

        Room currentRoom = currentCell.getRoom();
        if (currentRoom == null || !secretPassages.containsKey(currentRoom)) {
            return false;
        }

        Room targetRoom = secretPassages.get(currentRoom);

        // Find secret passage cell in target room
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                GameBoardCell cell = grid[x][y];
                if (cell.getCellType() == GameBoardCell.CellType.SECRET_PASSAGE &&
                        cell.getRoom() == targetRoom) {
                    movePlayer(player, x, y);
                    return true;
                }
            }
        }
        return false;
    }

    public void displayGameBoard(List<Player> players) {
        System.out.println("\n=== CLUE GAME BOARD ===");


        System.out.print("  ");
        for (int i = 0; i < WIDTH; i++) {
                System.out.print(i % 10 + " ");

        }
        System.out.println();

        for (int y = 0; y < HEIGHT; y++) {
            System.out.print(y % 10 + " ");

            for (int x = 0; x < WIDTH; x++) {

                GameBoardCell cell = grid[x][y];
                char symbol = getSymbol(cell);
                String color = getColor(cell, players, x, y);

                System.out.print(color + symbol + RESET + " ");
            }

            System.out.println();
        }

        displayLegend();
    }

    private char getSymbol(GameBoardCell cell) {
        char retChar;
        switch (cell.getCellType()) {
            case HALLWAY -> retChar ='.';
            case ROOM ->  retChar=cell.getRoom().getName().charAt(0);
            case WALL -> retChar ='|';
            case SECRET_PASSAGE -> retChar ='G';
            case DOOR -> retChar ='D';
            default -> retChar ='?';

        };

        return retChar;
    }

    private String getColor(GameBoardCell cell, List<Player> players, int x, int y) {
        for (Player p : players) {
            if (p.getX() == x && p.getY() == y) {
                return switch (p.getCharacter()) {
                    case "Red" -> RED;
                    case "Yellow" -> YELLOW;
                    case "White" -> WHITE;
                    case "Green" -> GREEN;
                    case "Blue" -> BLUE;
                    case "Purple" -> PURPLE;
                    default -> "";
                };
            }
        }
        return "";
    }

    private void displayLegend() {
        System.out.println("\nLEGEND:");
        System.out.println(RED + "S" + RESET + " Miss Scarlet (Red)");
        System.out.println(YELLOW + "O" + RESET + " Colonel Mustard (Yellow)");
        System.out.println(WHITE + "W" + RESET + " Mrs. White (White)");
        System.out.println(GREEN + "G" + RESET + " Mr. Green (Green)");
        System.out.println(BLUE + "P" + RESET + " Mrs. Peacock (Blue)");
        System.out.println(PURPLE + "M" + RESET + " Professor Plum (Purple)");
        System.out.println("R - Room  · - Hallway  █ - Wall  D - Door  G - Secret Passage");
    }
}