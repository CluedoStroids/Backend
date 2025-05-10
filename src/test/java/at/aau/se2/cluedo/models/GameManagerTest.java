package at.aau.se2.cluedo.models;

import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.cards.CardType;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gamemanager.GameState;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GameManagerTest {

    private GameManager gameManager;

    @BeforeEach
    void setUp() {
        gameManager = new GameManager(6);
    }

    @Test
    void testGameInitialization() {
        assertEquals(GameState.INITIALIZED, gameManager.getState());
        assertNotNull(gameManager.getSecretFile());
        assertEquals(0, gameManager.getCurrentPlayerIndex());
        assertTrue(gameManager.getPlayers().get(0).isCurrentPlayer());
    }

    @Test
    void testCardDistribution() {
        int totalCards = 0;
        for (Player player : gameManager.getPlayers()) {
            totalCards += player.getCards().size();
        }
        assertEquals(18, totalCards);
    }

    @Test
    void testInitializeGameGeneratesSecretFile() {
        SecretFile secret = gameManager.getSecretFile();
        assertNotNull(secret);
        assertNotNull(secret.room());
        assertNotNull(secret.weapon());
        assertNotNull(secret.character());
    }

    @Test
    void testAddAndGetPlayers() {
        Player player = new Player("Markus","Markus",2,5, PlayerColor.GREEN);
        assertEquals(6, gameManager.getPlayers().size());
    }

    @Test
    void testNextTurnAndTopOfRound() {
        gameManager.nextTurn();
        assertEquals("Colonel Mustard", gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()).getName());

        gameManager.nextTurn();
        gameManager.nextTurn();
        gameManager.nextTurn();
        gameManager.nextTurn();
        gameManager.nextTurn();
        gameManager.nextTurn(); // Should wrap around to 0
        assertEquals("Colonel Mustard", gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()).getName());
    }


    @Test
    void testSolveFileCorrectly() {
        SecretFile correct = gameManager.getSecretFile();

        gameManager.makeAccusation(gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()),correct);
        assertTrue(gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()).hasWon());
    }

    @Test
    void testSolveFileIncorrectly() {

        SecretFile wrong = new SecretFile(
                new BasicCard("FakeRoom", UUID.randomUUID(), CardType.ROOM),
                new BasicCard("FakeWeapon", UUID.randomUUID(), CardType.WEAPON),
                new BasicCard("FakeChar", UUID.randomUUID(), CardType.CHARACTER)
        );gameManager.makeAccusation(gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()),wrong);

        assertFalse(gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()).isActive());

    }


    @Test
    void testGetACardReturnsBasicCard() {
        assertNotNull(gameManager.getCards().get(0));
        assertInstanceOf(BasicCard.class, gameManager.getCards().get(0));
    }

    @Test
    void testRandomDiceRollRange() {
        for (int i = 0; i < 100; i++) {
            int result = gameManager.rollDice();
            assertTrue(result >= 1 && result <= 6);
        }
    }
}