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

    public record SuggestionRecord(String suspect, String weapon) {}


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
        this("LEGACY", initializeDefaultPlayers().subList(0, count), false);
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

    public int performMovement(Player player, List<String> movement) {
        if (movement.isEmpty() || movement.size() > diceRollS) return 0;

        String move = movement.get(0);
        if (move.equalsIgnoreCase("X")) return 0;

        int newX = player.getX();
        int   newY = player.getY();

        switch (move.toUpperCase()) {
            case "W" -> newY--;
            case "S" -> newY++;
            case "A" -> newX--;
            case "D" -> newX++;
            default -> {
                logger.info("Invalid input!");
                return 0;
            }
        }

        for (Player p : players) {
            if (p != player && p.getX() == newX && p.getY() == newY) {
                logger.info("Invalid move - position occupied!");
                return 0;
            }
        }

        if (gameBoard.movePlayer(player, newX, newY, false)) {
            return performMovement(player, movement.subList(1, movement.size()));
        } else {
            logger.info("Invalid move - cannot move to that position!");
            return 0;
        }
    }

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

    public BasicCard getCardByName(String cardName) {
        for (BasicCard card : cards) {
            if (card.getCardName().equals(cardName)) return card;
        }
        return null;
    }

    public boolean makeAccusation(Player player, SecretFile accusation) {
        if (secretFile.room().cardEquals(accusation.room())
                && secretFile.character().cardEquals(accusation.character())
                && secretFile.weapon().cardEquals(accusation.weapon())) {
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

    public boolean inRoom(Player player) {
        GameBoardCell cell = gameBoard.getCell(player.getX(), player.getY());
        return cell != null && cell.getCellType() == CellType.ROOM;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void nextTurn() {
        players.get(currentPlayerIndex).setCurrentPlayer(false);
        int attempts = 0;

        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            attempts++;
            if (attempts >= players.size()) {
                logger.warn("No active players found, game should end");
                return;
            }
        } while (!players.get(currentPlayerIndex).isActive());

        players.get(currentPlayerIndex).setCurrentPlayer(true);
        logger.info("Next turn: {}", players.get(currentPlayerIndex).getName());
    }

    public void eliminateCurrentPlayer() {
        getCurrentPlayer().setActive(false);
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

    public Player getPlayer(String username) {
        for (Player p : players) {
            if (p.getName().equals(username)) return p;
        }
        return null;
    }


    public void reportCheating(String accuser, String suspect) {
        cheatingReports.putIfAbsent(suspect, new HashSet<>());
        cheatingReports.get(suspect).add(accuser);
    }

    public int getCheatingReportsCount(String suspect) {
        return cheatingReports.getOrDefault(suspect, Set.of()).size();
    }

    public void recordSuggestion(Player player, String suspect, String weapon) {
        lastSuggestions.put(player.getName(), new SuggestionRecord(suspect, weapon));
    }

    public SuggestionRecord getLastSuggestion(String playerName) {
        return lastSuggestions.get(playerName);
    }

    public void resetPlayer(Player player) {
        gameBoard.movePlayer(player, player.getStartX(), player.getStartY(), true);
    }

}
