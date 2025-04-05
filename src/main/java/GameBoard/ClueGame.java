package GameBoard;

import java.util.*;

public class ClueGame {
    private final ClueGameBoard gameBoard;
    private final List<Player> players;
    private int currentPlayerIndex;
    private final List<String> possibleSuspects;
    private final List<String> possibleWeapons;
    private final List<String> possibleRooms;
    private final Map<String, String> solution;
    private final Scanner scanner;

    public ClueGame() {
        this.gameBoard = new ClueGameBoard();
        this.players = initializePlayers();
        this.scanner = new Scanner(System.in);

        this.possibleSuspects = new ArrayList<>(Arrays.asList(
                "Miss Scarlet", "Colonel Mustard", "Mrs. White",
                "Mr. Green", "Mrs. Peacock", "Professor Plum"));

        this.possibleWeapons = new ArrayList<>(Arrays.asList(
                "Dagger", "Revolver", "Rope", "Lead Pipe", "Wrench", "Candlestick"));

        this.possibleRooms = new ArrayList<>(Arrays.asList(
                "Hall", "Lounge", "Dining Room", "Kitchen",
                "Ballroom", "Conservatory", "Library", "Study", "Billiard Room"));

        this.solution = determineSolution();
        distributeCards();

        this.currentPlayerIndex = 0;
        players.get(0).setCurrentPlayer(true);
    }

    private List<Player> initializePlayers() {
        return Arrays.asList(
                new Player("Miss Scarlet", "Red", 7, 24),
                new Player("Colonel Mustard", "Yellow", 0, 17),
                new Player("Mrs. White", "White", 9, 0),
                new Player("Mr. Green", "Green", 14, 0),
                new Player("Mrs. Peacock", "Blue", 23, 6),
                new Player("Professor Plum", "Purple", 23, 19)
        );
    }

    private Map<String, String> determineSolution() {
        Map<String, String> solution = new HashMap<>();
        Collections.shuffle(possibleSuspects);
        Collections.shuffle(possibleWeapons);
        Collections.shuffle(possibleRooms);

        solution.put("Suspect", possibleSuspects.get(0));
        solution.put("Weapon", possibleWeapons.get(0));
        solution.put("Room", possibleRooms.get(0));

        return solution;
    }

    private void distributeCards() {
        List<String> allCards = new ArrayList<>();
        allCards.addAll(possibleSuspects);
        allCards.addAll(possibleWeapons);
        allCards.addAll(possibleRooms);

        // Remove solution cards
        allCards.remove(solution.get("Suspect"));
        allCards.remove(solution.get("Weapon"));
        allCards.remove(solution.get("Room"));
        Collections.shuffle(allCards);

        // Distribute cards evenly
        int playerIndex = 0;
        for (String card : allCards) {
            players.get(playerIndex).addCard(card);
            playerIndex = (playerIndex + 1) % players.size();
        }
    }

    public void startGame() {
        System.out.println("Welcome to Clue!");

        while (true) {
            Player currentPlayer = players.get(currentPlayerIndex);
            playRound(currentPlayer);

            if (checkGameEnd()) {
                break;
            }

            nextPlayer();
        }
    }

    private void playRound(Player player) {
        System.out.println("\n" + player.getName() + "'s turn");
        gameBoard.displayGameBoard(this.players);

        // Roll dice and move
        int diceRoll = rollDice();
        System.out.println("You rolled a " + diceRoll + "!");

        for (int i = 0; i < diceRoll; i++) {
            if (!performMovement(player)) {
                break;
            }
        }

        // Room actions
        if (inRoom(player)) {
            roomActions(player);
        }
    }

    private boolean inRoom(Player player) {
        GameBoardCell cell = gameBoard.getCell(player.getX(), player.getY());
        return cell != null && cell.getCellType() == GameBoardCell.CellType.ROOM;
    }

    private void roomActions(Player player) {
        System.out.println("\nYou are in the " + gameBoard.getCell(player.getX(), player.getY()).getRoom().getName());
        System.out.println("1. Make a suggestion");
        System.out.println("2. Make an accusation");
        System.out.println("3. Do nothing");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> makeSuggestion(player);
            case 2 -> makeAccusation(player);
        }
    }

    private void makeSuggestion(Player player) {
        System.out.println("\nMake a suggestion:");
        System.out.println("Which suspect? " + possibleSuspects);
        String suspect = scanner.nextLine();

        System.out.println("Which weapon? " + possibleWeapons);
        String weapon = scanner.nextLine();

        System.out.println("Which room? (current room: " +
                gameBoard.getCell(player.getX(), player.getY()).getRoom().getName() + ")");
        String room = scanner.nextLine();

        // Gather evidence
        for (Player p : this.players) {
            if (p != player) {
                for (String card : p.getCards()) {
                    if (card.equals(suspect) || card.equals(weapon) || card.equals(room)) {
                        System.out.println(p.getName() + " shows you: " + card);
                        return;
                    }
                }
            }
        }
        System.out.println("No one could disprove your suggestion!");
    }

    private void makeAccusation(Player player) {
        System.out.println("\nMake an accusation:");
        System.out.println("Which suspect? " + possibleSuspects);
        String suspect = scanner.nextLine();

        System.out.println("Which weapon? " + possibleWeapons);
        String weapon = scanner.nextLine();

        System.out.println("Which room? " + possibleRooms);
        String room = scanner.nextLine();

        if (suspect.equals(solution.get("Suspect")) &&
                weapon.equals(solution.get("Weapon")) &&
                room.equals(solution.get("Room"))) {
            System.out.println("Correct! " + player.getName() + " has solved the crime!");
            player.setHasWon(true);
        } else {
            System.out.println("Wrong! " + player.getName() + " is out of the game!");
            player.setActive(false);
        }
    }

    private boolean performMovement(Player player) {
        System.out.print("Direction (W/A/S/D) or X to cancel: ");
        String input = scanner.nextLine().toUpperCase();

        if (input.equals("X")) {
            return false;
        }

        int newX = player.getX();
        int newY = player.getY();

        switch (input) {
            case "W" -> newY--;
            case "S" -> newY++;
            case "A" -> newX--;
            case "D" -> newX++;
            default -> {
                System.out.println("Invalid input!");
                return true;
            }
        }

        if (gameBoard.movePlayer(player, newX, newY)) {
            return true;
        } else {
            System.out.println("Invalid move!");
            return true;
        }
    }

    private int rollDice() {
        return (int)(Math.random() * 6) + 1;
    }

    private boolean checkGameEnd() {
        for (Player p : players) {
            if (p.hasWon()) {
                System.out.println(p.getName() + " has won!");
                return true;
            }
        }

        // Check if only one player remains active
        long activePlayers = players.stream().filter(Player::isActive).count();
        if (activePlayers <= 1) {
            System.out.println("Game over! No one solved the crime.");
            return true;
        }

        return false;
    }

    private void nextPlayer() {
        players.get(currentPlayerIndex).setCurrentPlayer(false);

        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (!players.get(currentPlayerIndex).isActive());

        players.get(currentPlayerIndex).setCurrentPlayer(true);
    }

    public static void main(String[] args) {
        new ClueGame().startGame();
    }
}