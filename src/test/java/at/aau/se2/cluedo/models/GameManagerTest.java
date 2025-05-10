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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GameManagerTest {

    private GameManager gameManager;

    @BeforeEach
    void setUp() {
        Player player1 = new Player("John","Mr. Green",10,10, PlayerColor.GREEN);
        Player player2 = new Player("Bob","Mrs. White",10,10, PlayerColor.WHITE);
        Player player3 = new Player("Foo","Colonel Mustard",10,10, PlayerColor.YELLOW);
        gameManager = new GameManager(List.of(player1, player2, player3));

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
    void testPlayerMovement(){
        Player p = gameManager.getCurrentPlayer();
        gameManager.performMovement(p, List.of("W","A","A"));
        assertEquals(7,p.getX());
        assertEquals(24,p.getY());
    }

    @Test
    void testGetPlayers() {
        assertEquals(3, gameManager.getPlayers().size());
    }

    @Test
    void testNextTurnAndTopOfRound() {
        gameManager.nextTurn();
        assertEquals("Bob", gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()).getName());

        gameManager.nextTurn();
        gameManager.nextTurn();
        gameManager.nextTurn();
        gameManager.nextTurn();
        gameManager.nextTurn();
        gameManager.nextTurn();
        assertEquals("Bob", gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()).getName());
    }

    @Test
    void testNextTurn() {
        assertEquals(0, gameManager.getCurrentPlayerIndex());
        assertTrue(gameManager.getPlayers().get(0).isCurrentPlayer());
        assertFalse(gameManager.getPlayers().get(1).isCurrentPlayer());

        gameManager.nextTurn();
        assertEquals(1, gameManager.getCurrentPlayerIndex());
        assertFalse(gameManager.getPlayers().get(0).isCurrentPlayer());
        assertTrue(gameManager.getPlayers().get(1).isCurrentPlayer());

        gameManager.getPlayers().get(2).setActive(false);
        gameManager.nextTurn();
        assertEquals(0, gameManager.getCurrentPlayerIndex());
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
    void testGetCardByNameReturnsCorrectCard() {
        BasicCard anyCard = gameManager.getCards().get(0);
        BasicCard foundCard = gameManager.getCardByName(anyCard.getCardName());

        assertNotNull(foundCard);
        assertEquals(anyCard.getCardName(), foundCard.getCardName());
    }

    @Test
    void testGetCardByNameReturnsNullIfNotFound() {
        BasicCard foundCard = gameManager.getCardByName("");
        assertNull(foundCard);
    }

    @Test
    void testGameEndsWhenOnlyOnePlayerRemains() {
        for (int i = 1; i < gameManager.getPlayers().size(); i++) {
            gameManager.getPlayers().get(i).setActive(false);
        }

        int activePlayers = (int) gameManager.getPlayers().stream().filter(Player::isActive).count();
        assertEquals(1, activePlayers);
    }

    @Test
    void testCheckGameEnd() {
        assertFalse(gameManager.checkGameEnd());

        gameManager.getPlayers().get(0).setHasWon(true);
        assertTrue(gameManager.checkGameEnd());
    }

    @Test
    void testCheckGameEndState() {
        assertFalse(gameManager.checkGameEnd());

        gameManager.setState(GameState.ENDED);
        assertTrue(gameManager.checkGameEnd());
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