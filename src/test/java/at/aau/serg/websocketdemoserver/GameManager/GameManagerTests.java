package at.aau.serg.websocketdemoserver.GameManager;

import at.aau.serg.websocketdemoserver.GameObjects.Cards.BasicCard;
import at.aau.serg.websocketdemoserver.GameObjects.Player;
import at.aau.serg.websocketdemoserver.GameObjects.SecretFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GameManagerTests {

    private GameManager gameManager;

    @BeforeEach
    void setUp() {
        gameManager = new GameManager();

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
    void testAddAndGetPlayer() {
        Player player = new Player();
        gameManager.addPlayer(player);
        assertEquals(player, gameManager.getPlayer(0));
    }

    @Test
    void testNextTurnAndTopOfRound() {
        Player player1 = new Player();
        Player player2 = new Player();
        gameManager.addPlayer(player1);
        gameManager.addPlayer(player2);

        gameManager.nextTurn();
        assertEquals(player2, gameManager.getCurrentPlayer());

        gameManager.nextTurn(); // Should wrap around to 0
        assertEquals(player1, gameManager.getCurrentPlayer());
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

        assertTrue(gameManager.solveFile(correct));
    }

    @Test
    void testSolveFileIncorrectly() {
        gameManager.InitilizeGame();
        SecretFile wrong = new SecretFile(
                new BasicCard("FakeRoom", UUID.randomUUID(), "desc", "Room"),
                new BasicCard("FakeWeapon", UUID.randomUUID(), "desc", "Weapon"),
                new BasicCard("FakeChar", UUID.randomUUID(), "desc", "Character")
        );

        assertFalse(gameManager.solveFile(wrong));
    }


    @Test
    void testGetACardReturnsBasicCard() {
        assertNotNull(gameManager.getACard());
        assertTrue(gameManager.getACard() instanceof BasicCard);
    }

    @Test
    void testRandomDiceRollRange() {
        for (int i = 0; i < 100; i++) {
            int result = gameManager.randomDice();
            assertTrue(result >= 1 && result <= 12);
        }
    }
}
