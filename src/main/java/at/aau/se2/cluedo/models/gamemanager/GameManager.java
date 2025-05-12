package at.aau.se2.cluedo.models.gamemanager;

import java.util.List;
import java.util.ArrayList;
import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.gameboard.CellType;
import at.aau.se2.cluedo.models.gameboard.GameBoard;
import at.aau.se2.cluedo.models.gameboard.GameBoardCell;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import at.aau.se2.cluedo.services.LobbyRegistry;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.*;


@Getter
@Setter
public class GameManager {

    private static final Logger logger = LoggerFactory.getLogger(LobbyRegistry.class);

    private final GameBoard gameBoard;
    private final List<Player> players;
    private List<BasicCard> cards;
    private SecretFile secretFile;

    private Player winner;
    private GameState state;

    private int currentPlayerIndex;
    private int diceRollS;

    public GameManager(int count) {
        this.gameBoard = new GameBoard();
        this.players = initializePlayers(count);
        this.cards = new ArrayList<>();
        this.secretFile = null;
        this.winner = null;
        this.state = GameState.NOT_INITIALIZED;

        this.currentPlayerIndex = 0;

        initilizeGame();
        players.get(0).setCurrentPlayer(true);
    }


    public GameManager(List<Player> lobbyPlayers) {
        this.gameBoard = new GameBoard();

        List<Player> defaultPlayers = initializeDefaultPlayers();

        List<Player> updatedPlayers = new ArrayList<>();

        for (Player player: lobbyPlayers) {
            for (Player defaultPlayer: defaultPlayers) {
                if(player.getColor() == defaultPlayer.getColor()) {
                    player.move(defaultPlayer.getX(), defaultPlayer.getY());
                    updatedPlayers.add(player);
                }
            }
        }

        this.players = updatedPlayers;
        this.cards = new ArrayList<>();
        this.secretFile = null;
        this.winner = null;
        this.state = GameState.NOT_INITIALIZED;

        this.currentPlayerIndex = 0;

        initilizeGame();
        players.get(0).setCurrentPlayer(true);
    }

    public void initilizeGame() {
        //Call GameBoard
        generateSecretFileAndCards();
        distributeCards();
        this.state = GameState.INITIALIZED;
    }
    public Player getPlayer(String username){
        for (Player p: players) {
            if(p.getName().equals(username)){
                return p;
            }
        }
        return null;
    }
    private List<Player> initializePlayers(int count) {
        return initializeDefaultPlayers().subList(0, count);
    }

    private List<Player> initializeDefaultPlayers() {
        //todo Players have to be initalized based on the color they picked?
        return Arrays.asList(
                new Player("Miss Scarlet", "Scarlet", 7, 24, PlayerColor.RED),
                new Player("Colonel Mustard", "Mustard", 0, 17, PlayerColor.YELLOW),
                new Player("Mrs. White", "White", 9, 0, PlayerColor.WHITE),
                new Player("Mr. Green", "Green", 14, 0, PlayerColor.GREEN),
                new Player("Mrs. Peacock", "Peacock", 24, 6, PlayerColor.BLUE),
                new Player("Professor Plum", "Plum", 24, 19, PlayerColor.PURPLE)
        );
    }

    /**
     * Generate the SecretFile containing 1 room, 1 weapon and 1 character.
     * Add all remaining cards and store them in the instnace variable cards.
     */
    private void generateSecretFileAndCards() {
        cards.clear();
        List<BasicCard> rooms = BasicCard.getRooms();
        List<BasicCard> weapons = BasicCard.getWeapons();
        List<BasicCard> characters = BasicCard.getCharacters();

        Collections.shuffle(rooms);
        Collections.shuffle(characters);
        Collections.shuffle(weapons);

        secretFile = new SecretFile(rooms.remove(0), weapons.remove(0), characters.remove(0));
        cards.addAll(rooms);
        cards.addAll(weapons);
        cards.addAll(characters);
    }

    /**
     * Shuffle the remaining cards and distribute them evenly to each player.
     */
    private void distributeCards() {
        Collections.shuffle(cards);

        int playerIndex = 0;
        for (BasicCard card : cards) {
            players.get(playerIndex).addCard(card);
            playerIndex = (playerIndex + 1) % players.size();
        }
    }

    public int rollDice() {
        return (int) (Math.random() * 6) + 1;
    }

    /**
     * Recursive function to perform movement on the gameboard.
     * @param player current player who is moving
     * @param movement List of moves the player takes
     * @return recursive call
     */
    public int performMovement(Player player,  List<String> movement) {

        if(movement.isEmpty()){
            return 0;
        }

        //todo prevents from cheating.
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
                if(!input.equals(movement.get(movement.size() - 1))){
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

    /**
     * Suggestion happening every round. Player suggest/accuses a character with a weapon in the current room pt gather intel/evidence
     * @param player current player
     * @param suspect suspected character
     * @param weapon suspected weapon
     */
    public void makeSuggestion(Player player,String suspect, String weapon) {

        //todo implement actually suggest function with user interaction
        BasicCard room = getCardByName(gameBoard.getCell(player.getX(), player.getY()).getRoom().getName());
        BasicCard suspectCard = getCardByName(suspect);
        BasicCard weaponCard = getCardByName(weapon);

        // Gather evidence
        for (Player p : this.players) {
            if (p != player) {
                for (BasicCard card : p.getCards()) {
                    if (card.equals(suspectCard) || card.equals(weaponCard) || card.equals(room)) {
                        System.out.println(p.getName() + " shows you: " + card);
                        return;
                    }
                }
            }
        }
        System.out.println("No one could disprove your suggestion!");
    }

    /**
     * returns a BasicCard object based on the corresponding card name if its in the current card list of the game. Otherwise null.
     * @param cardName
     * @return
     */
    public BasicCard getCardByName(String cardName){
        for (BasicCard card : cards) {
            if (card.getCardName().equals(cardName)) {
                return card;
            }
        }
        return null;
    }

    /**
     * Accusation to solve the SecretFile. If correct player wins the game, otherwise he gets eliminated
     * @param player current player
     * @param accusation suspected secret file
     */
    public void makeAccusation(Player player, SecretFile accusation) {

        if (secretFile.room().equals(accusation.room()) && secretFile.character().equals(accusation.character()) && secretFile.weapon().equals(accusation.weapon())) {
            logger.info("Correct! {} has solved the crime!", player.getName());
            player.setHasWon(true);
            this.winner = player;
            this.state = GameState.ENDED;
        } else {
            logger.info("Wrong! {} is out of the game!", player.getName());
            player.setActive(false);
        }
    }

    public boolean checkGameEnd() {
        if(state == GameState.ENDED){
            return true;
        }

        for (Player p : players) {
            if (p.hasWon()) {
                return true;
            }
        }

        // Check if only one player remains active
        long activePlayers = players.stream().filter(Player::isActive).count();
        if (activePlayers == 1) {
            return true;
        }

        return false;
    }

    /**
     * Returns True if the current player is in a room, else returns False.
     * @param player
     * @return
     */
    public boolean inRoom(Player player) {
        GameBoardCell cell = gameBoard.getCell(player.getX(), player.getY());
        return cell != null && cell.getCellType() == CellType.ROOM;
    }

    public Player getCurrentPlayer(){
        return this.players.get(this.currentPlayerIndex);
    }

    /**
     * increments the currentPlayerIndex indicating the next turn
     */
    public void nextTurn() {
        players.get(currentPlayerIndex).setCurrentPlayer(false);

        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (!players.get(currentPlayerIndex).isActive());

        if (currentPlayerIndex >= players.size())
            this.currentPlayerIndex = 0;

        players.get(currentPlayerIndex).setCurrentPlayer(true);
    }
    /**
     * This method is a placeholder for getting input in the WebSocket version.
     * In a real implementation, this would be replaced with WebSocket communication.
     */
    private String getConsoleInputNextLine() {
        // In a WebSocket environment, this would be handled differently
        // For now, just return a default value
        return "3"; // Default to "do nothing" for room actions
    }

    public void eliminateCurrentPlayer() {
        Player current = getCurrentPlayer();
        current.setActive(false);
    }

    public String getCorrectSuspect() {
        return secretFile.character().getCardName();
    }

    public String getCorrectRoom() {
        return secretFile.room().getCardName();
    }

    public String getCorrectWeapon() {
        return secretFile.weapon().getCardName();
    }

    public List<Player> getPlayerList() {
        return new ArrayList<>(players);
    }



}
