package at.aau.se2.cluedo.models.gamemanager;

import at.aau.se2.cluedo.models.Random;
import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.gameboard.CellType;
import at.aau.se2.cluedo.models.gameboard.GameBoard;
import at.aau.se2.cluedo.models.gameboard.GameBoardCell;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Getter
@Setter
public class GameManager {

    private static final Logger logger = LoggerFactory.getLogger(GameManager.class);

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

    /**
     * retrieve the next player thats follows on the given one (by username).
     * @param username
     * @return
     */
    public Player getNextPlayer(String username){

        for (int playerIndex = 0; playerIndex<players.size(); playerIndex++) {
            String playerName = players.get(playerIndex).getName();
            if(playerName.equals(username)){
                return players.get((playerIndex+1)%players.size());
            }
        }

        return null;
    }

    private List<Player> initializePlayers(int count) {
        return initializeDefaultPlayers().subList(0, count);
    }

    private List<Player> initializeDefaultPlayers() {
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
        return Random.rand(6,1);
    }

    /**
     * Recursive function to perform movement on the gameboard.
     * @param player current player who is moving
     * @param movement List of moves the player takes
     * @return recursive call
     */
    public int performMovement(Player player, List<String> movement) {

        // Prevent cheating by limiting moves to dice roll and check if there are no more moves
        if (movement.isEmpty()||movement.size() > diceRollS) {
            return 0;
        }

        String currentMove = movement.get(0);

        // Handle exit command
        if (currentMove.equalsIgnoreCase("X")) {
            return 0;
        }

        int newX = player.getX();
        int newY = player.getY();

        // Calculate new position based on input
        switch (currentMove.toUpperCase()) {
            case "W" -> newY--;
            case "S" -> newY++;
            case "A" -> newX--;
            case "D" -> newX++;
            default -> {
                logger.info("Invalid input!");
                return 0;
            }
        }

        // Check for collision with other players
        for (Player p : players) {
            if (p != player && p.getX() == newX && p.getY() == newY) {
                logger.info("Invalid move - position occupied!");
                return 0;
            }
        }

        // Attempt to move the player
        if (gameBoard.movePlayer(player, newX, newY, false)) {
            // Move successful, process remaining movements
            return performMovement(player, movement.subList(1, movement.size()));
        } else {
            // Move failed (likely out of bounds or invalid position)
            logger.info("Invalid move - cannot move to that position!");
            return 0;
        }
    }

    /**
     * Suggestion happening every round. Player suggest/accuses a character with a weapon in the current room pt gather intel/evidence
     * @param player current player
     * @param suspect suspected character
     * @param weapon suspected weapon
     */
    public boolean makeSuggestion(Player player,String suspect, String weapon) {

        BasicCard room = getCardByName(gameBoard.getCell(player.getX(), player.getY()).getRoom().getName());
        BasicCard suspectCard = getCardByName(suspect);
        BasicCard weaponCard = getCardByName(weapon);

        // Gather evidence
        for (Player p : this.players) {
            if (p != player) {
                for (BasicCard card : p.getCards()) {
                    if (card.cardEquals(suspectCard) || card.cardEquals(weaponCard) || card.cardEquals(room)) {
                        logger.info("{} shows you: {}", p.getName(), card);
                        return true;
                    }
                }
            }
        }
        logger.info("No one could disprove your suggestion!");
        return false;
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
    public boolean makeAccusation(Player player, SecretFile accusation) {

        if (secretFile.room().cardEquals(accusation.room()) && secretFile.character().cardEquals(accusation.character()) && secretFile.weapon().cardEquals(accusation.weapon())) {
            logger.info("Correct! {} has solved the crime!", player.getName());
            player.setHasWon(true);
            this.winner = player;
            this.state = GameState.ENDED;
            return true;
        } else {
            logger.info("Wrong! {} is out of the game!", player.getName());
            player.setActive(false);
            return false;
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
        return activePlayers == 1;
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

        int attempts = 0;
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            attempts++;
            
            // Prevent infinite loop if no active players remain
            if (attempts >= players.size()) {
                logger.warn("No active players found, game should end");
                return;
            }
        } while (!players.get(currentPlayerIndex).isActive());

        if (currentPlayerIndex >= players.size())
            this.currentPlayerIndex = 0;
        logger.info(String.format("Next turn: %s",players.get(currentPlayerIndex).getName()));
        players.get(currentPlayerIndex).setCurrentPlayer(true);
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
