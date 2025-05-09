package at.aau.se2.cluedo.models.gameboard;

import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Initialization
        GameBoard gameBoard = new GameBoard();
        List<Player> players = createPlayers();
        Scanner scanner = new Scanner(System.in);

        // Main game loop
        while (true) {
            for (Player currentPlayer : players) {
                gameBoard.displayGameBoard(players);
                System.out.println("\n" + currentPlayer.getName() + "'s turn");

                // Movement
                System.out.print("Direction (W/A/S/D): ");
                String input = scanner.next().toUpperCase();

                int newX = currentPlayer.getX();
                int newY = currentPlayer.getY();

                switch (input) {
                    case "W" -> newY--;
                    case "S" -> newY++;
                    case "A" -> newX--;
                    case "D" -> newX++;
                    default -> System.out.println("Invalid input!");
                }

                currentPlayer.move(newX, newY);
            }
        }
    }

    private static List<Player> createPlayers() {
        return Arrays.asList(
                new Player("Miss Scarlet", "Scarlet", 7, 24, PlayerColor.RED),
                new Player("Colonel Mustard", "Mustard", 0, 17, PlayerColor.YELLOW),
                new Player("Mrs. White", "White", 9, 0, PlayerColor.WHITE),
                new Player("Mr. Green", "Green", 14, 0, PlayerColor.GREEN),
                new Player("Mrs. Peacock", "Peacock", 24, 6, PlayerColor.BLUE),
                new Player("Professor Plum", "Plum", 24, 19, PlayerColor.PURPLE)
        );
    }
}