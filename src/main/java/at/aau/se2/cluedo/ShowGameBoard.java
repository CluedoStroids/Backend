package at.aau.se2.cluedo;

import at.aau.se2.cluedo.models.gameboard.GameBoard;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;

import java.util.ArrayList;
import java.util.List;

public class ShowGameBoard {

    public static void main(String[] args) {
        GameBoard gm = new GameBoard();
       List<Player> players = new ArrayList<>();
       players.add(new Player("Hi","portz",4,5, PlayerColor.RED));
        gm.displayGameBoard(players);
    }
}
