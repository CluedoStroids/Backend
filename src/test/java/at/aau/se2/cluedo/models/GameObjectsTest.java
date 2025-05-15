package at.aau.se2.cluedo.models;

import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.cards.CardType;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GameObjectsTest {

    private Player player;

    @BeforeEach
    public void setUp() {
        // Create a new Player for each test
        player = new Player("John", "Scarlett", 0, 0, PlayerColor.RED);
    }

    @Test
    public void testAddCard() {
        // Create a BasicCard with a unique UUID and add it to the player
        UUID cardID = UUID.randomUUID();
        BasicCard card = new BasicCard("Revolver", cardID, CardType.WEAPON);

        // Add the card to the player
        player.addCard(card);

        // Assert the player has the card
        assertTrue(player.getCards().contains(card), "Player should have the Revolver card.");
    }

    @Test
    public void testHasCard() {
        // Create a BasicCard and add it to the player
        UUID cardID = UUID.randomUUID();
        BasicCard card = new BasicCard("Revolver", cardID, CardType.WEAPON);
        player.addCard(card);

        // Assert the player has the card by name
        assertTrue(player.hasCard(card), "Player should have the Revolver card.");
    }

    @Test
    public void testHasCardReturnsFalseWhenCardNotPresent() {
        // Create a card but don't add it to the player
        BasicCard card = new BasicCard("Candlestick", UUID.randomUUID(), CardType.WEAPON);

        // Assert that the player doesn't have the "Candlestick" card
        assertFalse(player.hasCard(card), "Player should not have the Candlestick card.");
    }

    @Test
    public void testMovePlayer() {
        // Move the player to new coordinates
        player.move(5, 5);

        // Assert the player has moved to the new position
        assertEquals(5, player.getX(), "Player's X coordinate should be 5.");
        assertEquals(5, player.getY(), "Player's Y coordinate should be 5.");
    }

    @Test
    public void testSetCurrentPlayer() {
        // Set the player as the current player
        player.setCurrentPlayer(true);

        // Assert the player is the current player
        assertTrue(player.isCurrentPlayer(), "Player should be the current player.");
    }

    @Test
    public void testPlayerHasWon() {
        // Set the player as having won
        player.setHasWon(true);

        // Assert the player has won
        assertTrue(player.hasWon(), "Player should have won.");
    }

    @Test
    public void testSetActive() {
        // Set the player to inactive
        player.setActive(false);

        // Assert the player is inactive
        assertFalse(player.isActive(), "Player should be inactive.");
    }
}
