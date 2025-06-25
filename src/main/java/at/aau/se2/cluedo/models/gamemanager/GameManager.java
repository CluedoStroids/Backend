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

import java.util.*;

@Getter
@Setter
public class GameManager {

    private static final Logger logger = LoggerFactory.getLogger(GameManager.class);

    private final String lobbyId;
    private final GameBoard gameBoard;
    private final List<Player> players;
    private List<BasicCard> cards;
    private SecretFile secretFile;
    private Player winner;
    private GameState state;
    private int currentPlayerIndex;
    private int diceRollS;
    private final Map<String, Set<String>> cheatingReports = new HashMap<>();
    private final Map<String, SuggestionRecord> lastSuggestions = new HashMap<>();

    public record SuggestionRecord(String suspect, String room, String weapon) {
    }


    public void recordSuggestion(Player player, String suspect, String room, String weapon) {
        lastSuggestions.put(player.getName(), new SuggestionRecord(suspect, room, weapon));

        if (!room.equals(player.getCurrentRoom())) {
            player.setCurrentRoom(room);
            player.resetSuggestionsInCurrentRoom();
        }

        player.incrementSuggestionsInCurrentRoom();
    }


    private GameManager(String lobbyId, List<Player> inputPlayers, boolean fromLobby) {
        this.lobbyId = lobbyId;
        this.gameBoard = new GameBoard();
        this.players = fromLobby ? mapLobbyPlayers(inputPlayers) : inputPlayers;
        this.cards = new ArrayList<>();
        this.secretFile = null;
        this.winner = null;
        this.state = GameState.NOT_INITIALIZED;
        this.currentPlayerIndex = 0;

        initializeGame();
        if (!this.players.isEmpty()) {
            this.players.get(0).setCurrentPlayer(true);
        }
    }

    public GameManager(int count) {
        this("LEGACY", GameManager.initializeDefaultPlayers().subList(0, count), false);
    }

    public GameManager(List<Player> lobbyPlayers) {
        this("LEGACY", lobbyPlayers, true);
    }

    public GameManager(String lobbyId, List<Player> lobbyPlayers) {
        this(lobbyId, lobbyPlayers, true);
    }

    private List<Player> mapLobbyPlayers(List<Player> lobbyPlayers) {
        List<Player> defaultPlayers = initializeDefaultPlayers();
        List<Player> updatedPlayers = new ArrayList<>();
        for (Player player : lobbyPlayers) {
            for (Player def : defaultPlayers) {
                if (player.getColor() == def.getColor()) {
                    player.move(def.getX(), def.getY());
                    updatedPlayers.add(player);
                }
            }
        }
        return updatedPlayers;
    }

    public Player getPlayer(String username) {
        for (Player p : players) {
            if (p.getName().equals(username)) {
                return p;
            }
        }
        return null;
    }


    private static List<Player> initializeDefaultPlayers() {
        return Arrays.asList(
                new Player("Miss Scarlet", "Scarlet", 7, 24, PlayerColor.RED),
                new Player("Colonel Mustard", "Mustard", 0, 17, PlayerColor.YELLOW),
                new Player("Mrs. White", "White", 9, 0, PlayerColor.WHITE),
                new Player("Mr. Green", "Green", 14, 0, PlayerColor.GREEN),
                new Player("Mrs. Peacock", "Peacock", 24, 6, PlayerColor.BLUE),
                new Player("Professor Plum", "Plum", 24, 19, PlayerColor.PURPLE)
        );
    }

    public void initializeGame() {
        generateSecretFileAndCards();
        distributeCards();
        this.state = GameState.INITIALIZED;
        resetPlayerReporting();
    }

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

    private void distributeCards() {
        Collections.shuffle(cards);
        int playerIndex = 0;
        for (BasicCard card : cards) {
            players.get(playerIndex).addCard(card);
            playerIndex = (playerIndex + 1) % players.size();
        }
    }

    public int rollDice() {
        return Random.rand(6, 1);
    }

    /**
     * Recursive function to perform movement on the gameboard.
     *
     * @param player   current player who is moving
     * @param movement List of moves the player takes
     * @return recursive call
     */
    public int performMovement(Player player, List<String> movement) {

        // Prevent cheating by limiting moves to dice roll and check if there are no more moves
        if (movement.isEmpty() || movement.size() > diceRollS) {
            return 0;
        }

        String currentMove = movement.get(0);

        // Handle exit command
        if (currentMove.equalsIgnoreCase("X")) {
            return 0;
        }

        String move = movement.get(0);
        if (move.equalsIgnoreCase("X")) return 0;

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
     *
     * @param player  current player
     * @param suspect suspected character
     * @param weapon  suspected weapon
     */
    public boolean makeSuggestion(Player player, String suspect, String weapon) {

        BasicCard room = getCardByName(gameBoard.getCell(player.getX(), player.getY()).getRoom().getName());
        BasicCard suspectCard = getCardByName(suspect);
        BasicCard weaponCard = getCardByName(weapon);

        for (Player p : players) {
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
     *
     * @param cardName
     * @return
     */
    public BasicCard getCardByName(String cardName) {
        for (BasicCard card : cards) {
            if (card.getCardName().equals(cardName)) {
                return card;
            }
        }
        return null;
    }

    /**
     * Accusation to solve the SecretFile. If correct player wins the game, otherwise he gets eliminated
     *
     * @param player     current player
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
        if (state == GameState.ENDED) return true;

        if (players.stream().anyMatch(Player::hasWon)) return true;

        return players.stream().filter(Player::isActive).count() == 1;

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

    public String getCurrentRoom(Player player) {
        GameBoardCell cell = gameBoard.getCell(player.getX(), player.getY());
        return (cell != null && cell.getRoom() != null) ? cell.getRoom().getName() : null;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
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
        logger.info("Next turn: " + players.get(currentPlayerIndex).getName());
        players.get(currentPlayerIndex).setCurrentPlayer(true);
        players.get(currentPlayerIndex).resetReportAbility();
        logger.info("Next turn: {}", players.get(currentPlayerIndex).getName());
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


    public void reportCheating(String accuser, String suspect) {
        cheatingReports.putIfAbsent(suspect, new HashSet<>());
        cheatingReports.get(suspect).add(accuser);
    }

    public int getCheatingReportsCount(String suspect) {
        return cheatingReports.getOrDefault(suspect, Set.of()).size();
    }

    public SuggestionRecord getLastSuggestion(String playerName) {
        return lastSuggestions.get(playerName);
    }

    public void resetPlayer(Player player) {
        player.move(player.getStartX(), player.getStartY());
    }

    public void resetPlayerReporting() {
        for (Player player : players) {
            player.resetReportAbility();
        }
    }


    public boolean hasPlayerLeftRoom(Player player, String room) {
        SuggestionRecord last = lastSuggestions.get(player.getName());
        String current = getCurrentRoom(player);
        if (last == null || current == null) return false;
        return !current.equals(room);
    }

    public Player getNextPlayer(String currentPlayerName) {
        int index = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(currentPlayerName)) {
                index = i;
                break;
            }
        }

        if (index == -1) return null;

        int nextIndex = (index + 1) % players.size();

        for (int i = 0; i < players.size(); i++) {
            Player next = players.get((index + 1 + i) % players.size());
            if (next.isActive()) {
                return next;
            }
        }

        return players.get(nextIndex);
    }

}
