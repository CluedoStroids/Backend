package at.aau.se2.cluedo.models;

import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameboard.CellType;
import at.aau.se2.cluedo.models.gameboard.GameBoardCell;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GameManagerTest {

    private GameManager gameManager;

    @BeforeEach
    void setUp() {
        gameManager = new GameManager("6");
        gameManager.initilizeGame();

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
    void testGameManagerInitializesCorrectly() {
        assertNotNull(gameManager.getGameBoard());
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
        gameManager.initilizeGame();
        int distributed = gameManager.getPlayers().stream().mapToInt(p -> p.getCards().size()).sum();
        assertEquals(totalCards, distributed);
    }

    @Test
    @Disabled
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
    @Disabled
    void testSuggestionWithDisproof() {
        Player current = gameManager.getPlayers().get(0);
        Player next = gameManager.getPlayers().get(1);

        BasicCard room = new BasicCard("Library", UUID.randomUUID(), "", "Room");
        BasicCard weapon = new BasicCard("Gun", UUID.randomUUID(), "", "Weapon");
        next.addCard(room);

        current.setX(0);
        current.setY(0);

        GameBoardCell cell = gameManager.getGameBoard().getCell(0, 0);
        cell.setCellType(CellType.ROOM);
        cell.getRoom().setName("Library");

        gameManager.makeSuggestion(current, "Unknown", "Gun");
    }

    @Test
    void testGenerateCardsCreatesAllCardTypes() {
        gameManager.getRooms().clear();
        gameManager.getWeapons().clear();
        gameManager.getCharacter().clear();

        gameManager.initilizeGame();

        assertFalse(gameManager.getRooms().isEmpty());
        assertFalse(gameManager.getWeapons().isEmpty());
        assertFalse(gameManager.getCharacter().isEmpty());
    }
}
