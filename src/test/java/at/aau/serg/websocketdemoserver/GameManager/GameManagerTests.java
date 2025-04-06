package at.aau.serg.websocketdemoserver.GameManager;

import at.aau.serg.websocketdemoserver.GameObjects.Cards.BasicCard;
import at.aau.serg.websocketdemoserver.GameObjects.Player;
import at.aau.serg.websocketdemoserver.GameObjects.SecretFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GameManagerTests {

    private ClueGame gameManager;

    @BeforeEach
    void setUp() {
        gameManager = new ClueGame();

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

    }

    @Test
    void testAddAndGetPlayers() {
        Player player = new Player("Markus","Green",2,5);
        assertEquals(6, gameManager.getPlayers().size());
    }

    @Test
    void testNextTurnAndTopOfRound() {
        gameManager.nextTurn();
        assertEquals("Colonel Mustard", gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()));

        gameManager.nextTurn(); // Should wrap around to 0
        assertEquals("Mrs. White", gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()));
    }

    @Test
    void testInitializeGameGeneratesSecretFile() {
        gameManager.InitilizeGame();
        SecretFile secret = gameManager.getSecretFile();
        assertNotNull(secret);
        assertNotNull(secret.room());
        assertNotNull(secret.weapon());
        assertNotNull(secret.character());
    }

    @Test
    void testSolveFileCorrectly() {
        gameManager.InitilizeGame();
        SecretFile correct = gameManager.getSecretFile();

        gameManager.makeAccusation(gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()),correct);
        assertTrue(gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()).hasWon());
    }

    @Test
    void testSolveFileIncorrectly() {
        gameManager.InitilizeGame();
        SecretFile wrong = new SecretFile(
                new BasicCard("FakeRoom", UUID.randomUUID(), "desc", "Room"),
                new BasicCard("FakeWeapon", UUID.randomUUID(), "desc", "Weapon"),
                new BasicCard("FakeChar", UUID.randomUUID(), "desc", "Character")
        );gameManager.makeAccusation(gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()),wrong);

        assertFalse(gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()).isActive());

    }


    @Test
    void testGetACardReturnsBasicCard() {
        assertNotNull(gameManager.getCards().get(0));
        assertTrue(gameManager.getCards().get(0) instanceof BasicCard);
    }

    @Test
    void testRandomDiceRollRange() {
        for (int i = 0; i < 100; i++) {
            int result = gameManager.rollDice();
            assertTrue(result >= 1 && result <= 6);
        }
    }
}
