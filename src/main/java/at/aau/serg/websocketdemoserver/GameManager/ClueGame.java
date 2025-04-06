package at.aau.serg.websocketdemoserver.GameManager;

import GameBoard.ClueGameBoard;
import GameBoard.GameBoardCell;
import at.aau.serg.websocketdemoserver.GameObjects.Player;
import at.aau.serg.websocketdemoserver.GameObjects.Cards.BasicCard;
import at.aau.serg.websocketdemoserver.GameObjects.SecretFile;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class ClueGame {
    private final ClueGameBoard gameBoard;
    private final List<Player> players;
    private int currentPlayerIndex;
    private final Scanner scanner;
    private Random rn = new Random();
    private SecretFile secretFile;
    private ArrayList<BasicCard> cards = new ArrayList<>();
    private ArrayList<BasicCard> rooms = new ArrayList<>();
    private ArrayList<BasicCard> weapons = new ArrayList<>();
    private  ArrayList<BasicCard> character = new ArrayList<>();

    public ClueGame() {
        this.gameBoard = new ClueGameBoard();

        this.players = initializePlayers();
        this.scanner = new Scanner(System.in);
        initilizeGame();
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
    //Generate the secret File.
    public void generateFile(){
        cards.clear();
        //pick room
        Collections.shuffle(rooms);
        Collections.shuffle(character);
        Collections.shuffle(weapons);

        secretFile = new SecretFile(rooms.remove(0),weapons.remove(0),character.remove(0));
        cards.addAll(rooms);
        cards.addAll(weapons);
        cards.addAll(character);
    }
    // Top of the Round so restarting at 0
    public void topOfTheRound(){
        currentPlayerIndex = 0;
    }


    private void distributeCards() {
        List<BasicCard> allCards = new ArrayList<>();
        allCards.addAll(rooms);
        allCards.addAll(weapons);
        allCards.addAll(character);

        // Remove solution cards
        allCards.remove(secretFile.character());
        allCards.remove(secretFile.weapon());
        allCards.remove(secretFile.room());
        Collections.shuffle(allCards);

        // Distribute cards evenly
        int playerIndex = 0;
        for (BasicCard card : allCards) {
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
        Collections.shuffle(rooms);
        BasicCard room = rooms.get(0);
        Collections.shuffle(weapons);
        BasicCard weapon = weapons.get(0);
        Collections.shuffle(rooms);
        BasicCard chara = character.get(0);

        switch (choice) {
            case 1 -> makeSuggestion(player);
            case 2 -> makeAccusation(player,new SecretFile(room,weapon,chara));
        }
    }

    public void makeSuggestion(Player player) {
        System.out.println("\nMake a suggestion:");
        System.out.println("Which suspect? " + rooms.toString());
        String suspect = scanner.nextLine();

        System.out.println("Which weapon? " + weapons.toString());
        String weapon = scanner.nextLine();

        System.out.println("Which room? (current room: " +
                gameBoard.getCell(player.getX(), player.getY()).getRoom().getName() + ")");
        String room = scanner.nextLine();

        // Gather evidence
        for (Player p : this.players) {
            if (p != player) {
                for (BasicCard card : p.getCards()) {
                    if (card.equals(suspect) || card.equals(weapon) || card.equals(room)) {
                        System.out.println(p.getName() + " shows you: " + card);
                        return;
                    }
                }
            }
        }
        System.out.println("No one could disprove your suggestion!");
    }

    public void makeAccusation(Player player, SecretFile acusation) {
        System.out.println("\nMake an accusation:");


        if (secretFile.room().equals(acusation.room()) && secretFile.character().equals(acusation.character()) && secretFile.weapon().equals(acusation.weapon())) {
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

    public int rollDice() {
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


    public void nextTurn(){
        currentPlayerIndex+=1;
        if(currentPlayerIndex >=players.size()){
            topOfTheRound();
        }
    }
    private void generateCards(){
        rooms.add(new BasicCard("Livingroom",UUID.randomUUID(),"Its a room","Room"));
        rooms.add(new BasicCard("Garage",UUID.randomUUID(),"Its a room","Room"));
        weapons.add(new BasicCard("Knife",UUID.randomUUID(),"Its a Knife","Weapon"));
        weapons.add(new BasicCard("Rope",UUID.randomUUID(),"Its a Knife","Weapon"));
        character.add(new BasicCard("Portz",UUID.randomUUID(),"Its Portz","Character"));
        character.add(new BasicCard("Mrs. White",UUID.randomUUID(),"Its Mrs White","Character"));
    }
    public void initilizeGame(){
        //Call GameBoard
        generateCards();
        generateFile();
        distributeCards();
    }
    private void nextPlayer() {
        players.get(currentPlayerIndex).setCurrentPlayer(false);
/*
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (!players.get(currentPlayerIndex).isActive());
*/
        currentPlayerIndex++;
        if(currentPlayerIndex>=players.size())
            topOfTheRound();
        players.get(currentPlayerIndex).setCurrentPlayer(true);
    }

    public static void main(String[] args) {
        new ClueGame().startGame();
    }
}