package at.aau.se2.cluedo.models.gameboard;


import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameBoard {
    // ANSI color codes
    public static final String RED = "\u001B[41m";
    public static final String YELLOW = "\u001B[43m";
    public static final String WHITE = "\u001B[47m";
    public static final String GREEN = "\u001B[42m";
    public static final String BLUE = "\u001B[44m";
    public static final String PURPLE = "\u001B[45m";
    public static final String RESET = "\u001B[0m";
    private static final int WIDTH = 25;
    private static final int HEIGHT = 25;
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

        setDoor(17, 16);
        setDoor(5, 19);
        setDoor(11, 18);
        setDoor(12, 18);
        setDoor(14, 20);
        setDoor(17, 21);


        //Test Door
        setDoor(6, 24);


        setHallway(9, 1);
        setHallway(8, 1);

        setHallway(14, 1);
        setHallway(15, 1);

        setHallway(6, 9);
        setHallway(7, 9);

        setHallway(17, 14);
        setHallway(17, 18);

        setHallway(7, 24);
        setHallway(0, 17);


        setHallway(9, 0);
        setHallway(14, 0);


        setHallway(24, 6);
        setHallway(24, 19);






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

    public boolean movePlayer(Player player, int newX, int newY) {
        GameBoardCell targetCell = getCell(newX, newY);
        GameBoardCell currCell = getCell(player.getX(), player.getY());

        if (targetCell == null || !targetCell.isAccessible()) {
            return false;
        }


        if(targetCell.getCellType() != currCell.getCellType() && targetCell.getCellType() != CellType.DOOR && currCell.getCellType() != CellType.DOOR && targetCell.getCellType() != CellType.SECRET_PASSAGE && currCell.getCellType() != CellType.SECRET_PASSAGE){
            return false;
        }

        if(targetCell.getCellType() == CellType.DOOR){

            GameBoardCell targetCellUp = getCell(newX, Math.max(0,newY+1));
            GameBoardCell targetCellDown = getCell(newX, Math.max(0,newY-1));
            GameBoardCell targetCellLeft = getCell(Math.max(0,newX-1), newY);
            GameBoardCell targetCellRight = getCell(Math.max(0,newX+1), newY);

            if(currCell.getX()-targetCell.getX() != 0){
                if(currCell.getCellType() != targetCellRight.getCellType() && targetCellRight.getCellType() != CellType.DOOR ){
                    newX += 1;
                }
                else if(currCell.getCellType() != targetCellLeft.getCellType() && targetCellLeft.getCellType() != CellType.DOOR ){
                    newX -= 1;
                }
                else if(currCell.getCellType() != targetCellDown.getCellType() && targetCellDown.getCellType() != CellType.DOOR ){
                    newY -= 1;
                }
                else if(currCell.getCellType() != targetCellUp.getCellType() && targetCellUp.getCellType() != CellType.DOOR ){
                    newY += 1;
                }
            }else{
                if(currCell.getCellType() != targetCellDown.getCellType() && targetCellDown.getCellType() != CellType.DOOR ){
                    newY -= 1;
                }
                else if(currCell.getCellType() != targetCellUp.getCellType() && targetCellUp.getCellType() != CellType.DOOR ){
                    newY += 1;
                }
                else if(currCell.getCellType() != targetCellRight.getCellType() && targetCellRight.getCellType() != CellType.DOOR ){
                    newX += 1;
                }
                else if(currCell.getCellType() != targetCellLeft.getCellType() && targetCellLeft.getCellType() != CellType.DOOR ){
                    newX -= 1;
                }
            }

        }



        // Set new position
        player.move(newX, newY);

        if(targetCell.getCellType() == CellType.SECRET_PASSAGE){
            if(!useSecretPassage(player)){
                return false;
            }
        }

        // Room logic
        if (currCell.getCellType() == CellType.ROOM) {
            currCell.getRoom().playerLeavesRoom(player);
        }

        if (targetCell.getCellType() == CellType.ROOM) {
            targetCell.getRoom().playerEntersRoom(player);
        }

        return true;
    }

    public boolean useSecretPassage(Player player) {

        GameBoardCell currentCell = getCell(player.getX(), player.getY());
        if (currentCell.getCellType() != CellType.SECRET_PASSAGE) {
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
                if (cell.getCellType() == CellType.SECRET_PASSAGE &&  cell.getRoom() == targetRoom) {

                        GameBoardCell targetCellUp = getCell(x,Math.max(0,y+1));
                        GameBoardCell targetCellDown = getCell(x, Math.max(0,y-1));
                        GameBoardCell targetCellLeft = getCell(Math.max(0,x-1), y);
                        GameBoardCell targetCellRight = getCell(Math.max(0,x+1), y);

                        if(targetCellUp != null && targetCellUp.isAccessible() && cell.getRoom() == currentRoom)
                            player.move(targetCellUp.getX(),targetCellUp.getY());
                        else if(targetCellDown != null &&targetCellDown.isAccessible()&& cell.getRoom() == currentRoom)
                            player.move(targetCellDown.getX(),targetCellDown.getY());
                        else if(targetCellLeft != null && targetCellLeft.isAccessible()&& cell.getRoom() == currentRoom)
                            player.move(targetCellLeft.getX(),targetCellLeft.getY());
                        else if(targetCellRight != null && targetCellRight.isAccessible()&& cell.getRoom() == currentRoom)
                            player.move(targetCellRight.getX(),targetCellRight.getY());

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

        displayLegend(players.size());
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
        ;

        return retChar;
    }

    private String getColor(GameBoardCell cell, List<Player> players, int x, int y) {
        for (Player p : players) {
            if (p.getX() == x && p.getY() == y) {
                return switch (p.getColor()) {
                    case RED -> RED;
                    case YELLOW -> YELLOW;
                    case WHITE -> WHITE;
                    case GREEN -> GREEN;
                    case BLUE -> BLUE;
                    case PURPLE -> PURPLE;
                    default -> "";
                };
            }
        }
        return "";
    }

    private void displayLegend(int size) {
        System.out.println("\nLEGEND:");
        for (int i = 0; i < size; i++) {
            switch (i) {
                case 0->System.out.println(RED + "S" + RESET + " Miss Scarlet (Red)");
                case 1->System.out.println(YELLOW + "O" + RESET + " Colonel Mustard (Yellow)");

                case 2->System.out.println(WHITE + "W" + RESET + " Mrs. White (White)");

                case 3->System.out.println(GREEN + "G" + RESET + " Mr. Green (Green)");

                case 4->System.out.println(BLUE + "P" + RESET + " Mrs. Peacock (Blue)");

                case 5->System.out.println(PURPLE + "M" + RESET + " Professor Plum (Purple)");

            }
        }

        System.out.println("R - Room  · - Hallway  █ - Wall  D - Door  G - Secret Passage");
    }
}