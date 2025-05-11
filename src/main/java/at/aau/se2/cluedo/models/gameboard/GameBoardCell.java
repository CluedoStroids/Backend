package at.aau.se2.cluedo.models.gameboard;

import lombok.Getter;
import lombok.Setter;

@Getter
public class GameBoardCell {
    private final int x;
    private final int y;
    @Setter
    private CellType cellType;
    @Setter
    private Room room;


    public GameBoardCell(int x, int y, CellType type) {
        this.x = x;
        this.y = y;
        this.cellType = type;
    }

    public boolean isAccessible() {

        return cellType != CellType.WALL;
    }


}
