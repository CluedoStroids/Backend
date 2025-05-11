package at.aau.se2.cluedo.models.gamemanager;

import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.gameboard.CellType;
import at.aau.se2.cluedo.models.gameboard.GameBoard;
import at.aau.se2.cluedo.models.gameboard.GameBoardCell;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class GameManager {
    private final GameBoard gameBoard;
    private final List<Player> players;
    private int currentPlayerIndex;
    private SecretFile secretFile;
    private ArrayList<BasicCard> cards = new ArrayList<>();
    private ArrayList<BasicCard> rooms = new ArrayList<>();
    private ArrayList<BasicCard> weapons = new ArrayList<>();
    private ArrayList<BasicCard> character = new ArrayList<>();
    private int diceRollS;

    public GameManager(int count) {
        this.gameBoard = new GameBoard();
        this.players = initializePlayers(count);
        initializeGame();
        this.currentPlayerIndex = 0;
        players.get(0).setCurrentPlayer(true);
    }


    public GameManager(List<Player> lobbyPlayers) {
        this.gameBoard = new GameBoard();

        List<Player> defaultPlayers = initializeDefaultPlayers(lobbyPlayers.size());

        List<Player> updatedPlayers = new ArrayList<>();


        for (int i = 0; i < lobbyPlayers.size(); i++) {
            Player lobbyPlayer = lobbyPlayers.get(i);
            Player defaultPlayer = defaultPlayers.get(i);

            lobbyPlayer.move(defaultPlayer.getX(), defaultPlayer.getY());
            updatedPlayers.add(lobbyPlayer);
        }

        this.players = updatedPlayers;
        initializeGame();
        this.currentPlayerIndex = 0;
        players.get(0).setCurrentPlayer(true);
    }

    public static void main(String[] args) {
        new GameManager(4).startGame();
    }

    private List<Player> initializePlayers(int count) {
        return initializeDefaultPlayers(count);
    }

    private List<Player> initializeDefaultPlayers(int count) {
        return Arrays.asList(
                new Player("Miss Scarlet", "Scarlet", 7, 24, PlayerColor.RED),
                new Player("Colonel Mustard", "Mustard", 0, 17, PlayerColor.YELLOW),
                new Player("Mrs. White", "White", 9, 0, PlayerColor.WHITE),
                new Player("Mr. Green", "Green", 14, 0, PlayerColor.GREEN),
                new Player("Mrs. Peacock", "Peacock", 24, 6, PlayerColor.BLUE),
                new Player("Professor Plum", "Plum", 24, 19, PlayerColor.PURPLE)
        ).subList(0, count);
    }

    //Generate the secret File.
    public void generateFile() {
        cards.clear();
        //pick room
        Collections.shuffle(rooms);
        Collections.shuffle(character);
        Collections.shuffle(weapons);

        secretFile = new SecretFile(rooms.remove(0), weapons.remove(0), character.remove(0));
        cards.addAll(rooms);
        cards.addAll(weapons);
        cards.addAll(character);
    }

    // Top of the Round so restarting at 0
    public void topOfTheRound() {
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
        System.out.println("Welcome to Cluedo!");


            Player currentPlayer = players.get(currentPlayerIndex);
            playRound(currentPlayer);

            if (checkGameEnd()) {
                System.out.println("Game Ended");
            }

            nextPlayer();

    }

    private void playRound(Player player) {
        System.out.println("\n" + player.getName() + "'s turn");
        gameBoard.displayGameBoard(this.players);

        System.out.println("Move or Perform Action");

        // Roll dice and move
        diceRollS = rollDice();
        System.out.println("You rolled a " + diceRollS + "!");



        // Room actions
        if (inRoom(player)) {
            roomActions(player);
        }
    }

    private boolean inRoom(Player player) {
        GameBoardCell cell = gameBoard.getCell(player.getX(), player.getY());
        return cell != null && cell.getCellType() == CellType.ROOM;
    }

    private void roomActions(Player player) {
        System.out.println("\nYou are in the " + gameBoard.getCell(player.getX(), player.getY()).getRoom().getName());
        System.out.println("1. Make a suggestion");
        System.out.println("2. Make an accusation");
        System.out.println("3. Do nothing");
        System.out.println("Please input the number of your choice:");
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

        }*/
    }

    public void makeSuggestion(Player player,String suspect, String weapon) {

        String room = gameBoard.getCell(player.getX(), player.getY()).getRoom().getName();
        Player sus = player;
        for(Player p : players) {
            if (Objects.equals(p.getName(), suspect))
                sus = p;
        }
        if(sus == player){
            System.out.println("There was an error with this suggestion!");
        }
        gameBoard.teleportPlayerToRoom(sus,gameBoard.getCell(player.getX(), player.getY()).getRoom());

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

    public void makeAccusation(Player player, SecretFile accusation) {

        if (secretFile.room().equals(accusation.room()) && secretFile.character().equals(accusation.character()) && secretFile.weapon().equals(accusation.weapon())) {
            System.out.println("Correct! " + player.getName() + " has solved the crime!");
            player.setHasWon(true);
        } else {
            System.out.println("Wrong! " + player.getName() + " is out of the game!");
            player.setActive(false);
        }
    }


    public int performMovement(Player player, List<String> movement) {

        if(movement.size() == 0){
            return 0;
        }

        if(movement.size() > diceRollS){
            return 0;
        }

        for (String input: movement) {

            if (input.equalsIgnoreCase("X")) {
                return 0;
            }

            int newX = player.getX();
            int newY = player.getY();

            switch (input.toUpperCase()) {
                case "W" -> newY--;
                case "S" -> newY++;
                case "A" -> newX--;
                case "D" -> newX++;
                default -> {
                    System.out.println("Invalid input!");
                    return 0;
                }
            }

            for(Player p : players){
                if(p.getX() == newX && p.getY() == newY){
                    System.out.println("Invalid move!");
                    return 0;
                }
            }

            if (gameBoard.movePlayer(player, newX, newY,false)) {
                if(input != movement.get(movement.size()-1)){
                    movement.subList(1,movement.size()-1);
                    continue;
                }
                return performMovement(player, movement.subList(1,movement.size()-1));
            } else {
                System.out.println("Invalid move!");
                return performMovement(player,movement);
            }
        }
        System.out.println("Invalid move!");
        return performMovement(player, movement);
    }

    public int rollDice() {
        return (int) (Math.random() * 6) + 1;
    }

    public boolean checkGameEnd() {
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

    public void nextTurn() {
        currentPlayerIndex += 1;
        if (currentPlayerIndex >= players.size()) {
            topOfTheRound();
        }
    }

    private void generateCards() {
        rooms.add(new BasicCard("Livingroom", UUID.randomUUID(), "Its a room", "Room"));
        rooms.add(new BasicCard("Garage", UUID.randomUUID(), "Its a room", "Room"));
        weapons.add(new BasicCard("Knife", UUID.randomUUID(), "Its a Knife", "Weapon"));
        weapons.add(new BasicCard("Rope", UUID.randomUUID(), "Its a Knife", "Weapon"));
        character.add(new BasicCard("Portz", UUID.randomUUID(), "Its Portz", "Character"));
        character.add(new BasicCard("Mrs. White", UUID.randomUUID(), "Its Mrs White", "Character"));
    }

    public void initializeGame() {
        //Call GameBoard
        generateCards();
        generateFile();
        distributeCards();
    }

    public void nextPlayer() {
        players.get(currentPlayerIndex).setCurrentPlayer(false);

        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (!players.get(currentPlayerIndex).isActive());

        if (currentPlayerIndex >= players.size())
            topOfTheRound();
        players.get(currentPlayerIndex).setCurrentPlayer(true);
    }
}
