package at.aau.se2.cluedo.models.gamemanager;

import at.aau.se2.cluedo.models.gameboard.GameBoard;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class GameManagerConsole {

    private static GameManager game;

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

    public void startGame(GameManager game) {
        System.out.println("Welcome to Cluedo!");

        playRound(game);

        if (game.checkGameEnd()) {
            System.out.println("Game Ended");
        }

        game.nextTurn();

    }

    private static void playRound(GameManager game){
        System.out.println("\n" + game.getCurrentPlayer().getName() + "'s turn");
        game.getGameBoard().displayGameBoard(game.getPlayers());

        System.out.println("Move or Perform Action");

        // Roll dice and move
        game.rollDice();
        System.out.println("You rolled a " + game.getDiceRollS() + "!");

        // Room actions
        if (game.inRoom(game.getCurrentPlayer())) {
            roomActions(game.getCurrentPlayer());
        }
    }

    private static void roomActions(Player player) {
        System.out.println("\nYou are in the " + game.getGameBoard().getCell(player.getX(), player.getY()).getRoom().getName());
        System.out.println("1. Make a suggestion");
        System.out.println("2. Make an accusation");
        System.out.println("3. Do nothing");
        System.out.println("Please input the number of your choice:");
        //put somewhere else
        /*
        int choice = 0;
        do {


            try {
                choice = Integer.parseInt(getConsoleInputNextLine());
            }catch (Exception e){
                System.out.println("Invalid input. Try again.");

            }
        }
        while (choice == 0);

        Collections.shuffle(rooms);
        BasicCard room = rooms.get(0);
        Collections.shuffle(weapons);
        BasicCard weapon = weapons.get(0);
        Collections.shuffle(rooms);
        BasicCard chara = character.get(0);

        switch (choice) {
            case 1 -> makeSuggestion(player);
            case 2 -> makeAccusation(player, new SecretFile(room, weapon, chara));
            case 3 -> {
                return;
            }
            default -> {
                System.out.println("Invalid input. Try again.");
                roomActions(player);
            }

        }

         */
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
