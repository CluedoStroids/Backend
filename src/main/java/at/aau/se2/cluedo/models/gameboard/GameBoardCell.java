package at.aau.se2.cluedo.models.gameboard;

public class GameBoardCell {
    private final int x;
    private final int y;
    private CellType cellType;
    private Room room;
    public GameBoardCell(int x, int y, CellType type) {
        this.x = x;
        this.y = y;
        this.cellType = type;
    }

    public boolean isAccessible() {
        boolean check = false;

        if(cellType != CellType.WALL)
            check = true;


        return check;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public CellType getCellType() {
        return cellType;
    }

    public void setCellType(CellType type) {
        this.cellType = type;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }


}
