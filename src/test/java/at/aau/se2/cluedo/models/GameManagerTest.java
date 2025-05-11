package at.aau.se2.cluedo.models;

import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GameManagerTest {

    private GameManager gameManager;

    @BeforeEach
    void setUp() {
        gameManager = new GameManager(6);
        gameManager.initializeGame();

        ArrayList<BasicCard> rooms = new ArrayList<>();
        rooms.add(new BasicCard("Kitchen", UUID.randomUUID(), "Kitchen", "Room"));
        rooms.add(new BasicCard("Library", UUID.randomUUID(), "Library", "Room"));

        ArrayList<BasicCard> weapons = new ArrayList<>();
        weapons.add(new BasicCard("Knife", UUID.randomUUID(), "Knife", "Weapon"));
        weapons.add(new BasicCard("Gun", UUID.randomUUID(), "Gun", "Weapon"));

        ArrayList<BasicCard> characters = new ArrayList<>();
        characters.add(new BasicCard("Red", UUID.randomUUID(), "Red", "Character"));
        characters.add(new BasicCard("White", UUID.randomUUID(), "White", "Character"));

        gameManager.setRooms(rooms);
        gameManager.setWeapons(weapons);
        gameManager.setCharacter(characters);
        gameManager.generateFile();
    }
    @Test
    void testConstructorWithPlayerCount() {
        GameManager gm = new GameManager(3);
        assertEquals(3, gm.getPlayers().size());
        assertEquals(0, gm.getCurrentPlayerIndex());
        assertTrue(gm.getPlayers().get(0).isCurrentPlayer());
    }

    @Test
    void testConstructorWithLobbyPlayers() {
        List<Player> lobbyPlayers = List.of(
                new Player("Test1", "Alias1", 0, 0, PlayerColor.RED),
                new Player("Test2", "Alias2", 0, 0, PlayerColor.YELLOW)
        );

        GameManager gm = new GameManager(lobbyPlayers);
        assertEquals(2, gm.getPlayers().size());
    }
    @Test
    void testGenerateCards() {
        GameManager gm = new GameManager(3);
        assertFalse(gm.getRooms().isEmpty());
        assertFalse(gm.getWeapons().isEmpty());
        assertFalse(gm.getCharacter().isEmpty());
    }

    @Test
    void testGenerateFile() {
        GameManager gm = new GameManager(3);
        gm.generateFile();
        assertNotNull(gm.getSecretFile());
        assertFalse(gm.getCards().contains(gm.getSecretFile().room()));
    }
    @Test
    void testStartGameDoesNotCrash() {
        GameManager gm = new GameManager(2);
        assertDoesNotThrow(gm::startGame);
    }

    @Test
    void testNextPlayerWrapsAround() {
        GameManager gm = new GameManager(2);
        gm.nextPlayer();
        assertEquals(1, gm.getCurrentPlayerIndex());
    }

    @Test
    void testCorrectAccusation() {
        GameManager gm = new GameManager(2);
        SecretFile correct = gm.getSecretFile();
        Player p = gm.getPlayers().get(0);
        gm.makeAccusation(p, correct);
        assertTrue(p.hasWon());
    }

    @Test
    void testIncorrectAccusation() {
        GameManager gm = new GameManager(2);
        Player p = gm.getPlayers().get(0);

        BasicCard wrongRoom = new BasicCard("WrongRoom", UUID.randomUUID(), "desc", "Room");
        BasicCard wrongWeapon = new BasicCard("WrongWeapon", UUID.randomUUID(), "desc", "Weapon");
        BasicCard wrongCharacter = new BasicCard("WrongChar", UUID.randomUUID(), "desc", "Character");

        SecretFile wrong = new SecretFile(wrongRoom, wrongWeapon, wrongCharacter);
        gm.makeAccusation(p, wrong);
        assertFalse(p.isActive());
    }

    @Test
    void testInvalidMovementBlockedByPlayer() {
        GameManager gm = new GameManager(2);
        gm.setDiceRollS(1);
        Player p1 = gm.getPlayers().get(0);
        Player p2 = gm.getPlayers().get(1);
        p2.move(p1.getX(), p1.getY() + 1);
        int result = gm.performMovement(p1, List.of("S"));
        assertEquals(0, result); // Blocked
    }
    @Test
    void testGameEndByWinning() {
        GameManager gm = new GameManager(2);
        Player p = gm.getPlayers().get(0);
        p.setHasWon(true);
        assertTrue(gm.checkGameEnd());
    }

    @Test
    void testGameEndByOnlyOneActivePlayer() {
        GameManager gm = new GameManager(2);
        gm.getPlayers().get(1).setActive(false);
        assertTrue(gm.checkGameEnd());
    }
    @Test
    void testRollDiceRange() {
        GameManager gm = new GameManager(2);
        for (int i = 0; i < 100; i++) {
            int roll = gm.rollDice();
            assertTrue(roll >= 1 && roll <= 6);
        }
    }


    @Test
    void testAddAndGetPlayers() {
        Player player = new Player("Markus","Markus",2,5, PlayerColor.GREEN);
        assertEquals(6, gameManager.getPlayers().size());
        assertEquals(0, gameManager.getCurrentPlayerIndex());
        assertTrue(gameManager.getPlayers().get(0).isCurrentPlayer());
    }

    @Test
    void testRollDiceWithinRangeMultipleTimes() {
        for (int i = 0; i < 50; i++) {
            int roll = gameManager.rollDice();
            assertTrue(roll >= 1 && roll <= 6);
        }
    }

    @Test
    void testTopOfRoundResetsIndex() {
        gameManager.setCurrentPlayerIndex(5);
        gameManager.topOfTheRound();
        assertEquals(0, gameManager.getCurrentPlayerIndex());
    }

    @Test
    void testNextPlayerWrapsCorrectly() {
        gameManager.setCurrentPlayerIndex(5);
        gameManager.nextTurn();
        assertEquals(0, gameManager.getCurrentPlayerIndex());
    }

    @Test
    void testSecretFileIsProperlyGenerated() {
        SecretFile file = gameManager.getSecretFile();
        assertNotNull(file);
        assertNotNull(file.room());
        assertNotNull(file.weapon());
        assertNotNull(file.character());
    }

    @Test
    @Disabled
    void testCardsAreDistributedEvenlyAmongPlayers() {
        int totalCards = gameManager.getCards().size();
        gameManager.initializeGame();
        int distributed = gameManager.getPlayers().stream().mapToInt(p -> p.getCards().size()).sum();
        assertEquals(totalCards, distributed);
    }

    @Test
    void testCorrectAccusationWinsGame() {
        SecretFile actual = gameManager.getSecretFile();
        Player player = gameManager.getPlayers().get(0);
        gameManager.makeAccusation(player, actual);
        assertTrue(player.hasWon());
    }

    @Test
    void testIncorrectAccusationRemovesPlayer() {
        Player player = gameManager.getPlayers().get(0);
        SecretFile wrong = new SecretFile(
                new BasicCard("WrongRoom", UUID.randomUUID(), "", "Room"),
                new BasicCard("WrongWeapon", UUID.randomUUID(), "", "Weapon"),
                new BasicCard("WrongChar", UUID.randomUUID(), "", "Character")
        );
        gameManager.makeAccusation(player, wrong);
        assertFalse(player.isActive());
        assertFalse(player.hasWon());
    }

    @Test
    void testGameEndsWhenOnePlayerRemains() {
        for (int i = 1; i < gameManager.getPlayers().size(); i++) {
            gameManager.getPlayers().get(i).setActive(false);
        }
        assertTrue(gameManager.checkGameEnd());
    }

    @Test
    void testGameEndsWhenAPlayerWins() {
        Player winner = gameManager.getPlayers().get(0);
        winner.setHasWon(true);
        assertTrue(gameManager.checkGameEnd());
    }

    @Test
    void testPerformMovementWithInvalidDirection() {
        Player player = gameManager.getPlayers().get(0);
        List<String> moves = List.of("Z");
        gameManager.setDiceRollS(1);
        int result = gameManager.performMovement(player, moves);
        assertEquals(0, result);
    }

    @Test
    void testMovementTooLong() {
        Player player = gameManager.getPlayers().get(0);
        List<String> moves = List.of("W", "W", "W", "W", "W", "W", "W");
        gameManager.setDiceRollS(3);
        assertEquals(0, gameManager.performMovement(player, moves));
    }

    @Test
    void testEmptyMovementInput() {
        Player player = gameManager.getPlayers().get(0);
        List<String> moves = new ArrayList<>();
        assertEquals(0, gameManager.performMovement(player, moves));
    }

    @Test
    void testGenerateCardsCreatesAllCardTypes() {
        gameManager.getRooms().clear();
        gameManager.getWeapons().clear();
        gameManager.getCharacter().clear();

        gameManager.initializeGame();

        assertFalse(gameManager.getRooms().isEmpty());
        assertFalse(gameManager.getWeapons().isEmpty());
        assertFalse(gameManager.getCharacter().isEmpty());
    }
    @Test
    public void testSkipInactivePlayers() {
        // Make player 1 inactive
        gameManager.getPlayers().get(1).setActive(false);

        // Start with player 0
        assertTrue(gameManager.getPlayers().get(0).isCurrentPlayer());

        // Call next player, should skip player 1
        gameManager.nextPlayer();

        // Player 2 should be current
        assertTrue(gameManager.getPlayers().get(2).isCurrentPlayer());
        assertEquals(2, gameManager.getCurrentPlayerIndex());
    }
    @Test
    public void testMakeSuggestion(){
        Player player = gameManager.getPlayers().get(1);
        player.move(2,2);
        gameManager.makeSuggestion(player,"Mr. White","Knife");
    }
}
