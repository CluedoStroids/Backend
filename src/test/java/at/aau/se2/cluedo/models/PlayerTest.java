package at.aau.se2.cluedo.models;

import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.cards.CardType;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.PlayerColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    private Player player;
    private BasicCard card;

    @BeforeEach
    void setUp() {
        player = new Player("TestPlayer", "Miss Scarlet", 5, 10, PlayerColor.RED);
        card = new BasicCard("Test Card", UUID.randomUUID(), CardType.CHARACTER);
    }

    @Test
    void testPlayerCreation() {
        assertEquals("TestPlayer", player.getName());
        assertEquals("Miss Scarlet", player.getCharacter());
        assertEquals(5, player.getX());
        assertEquals(10, player.getY());
        assertNotNull(player.getPlayerID());
        assertTrue(player.isActive());
        assertFalse(player.isCurrentPlayer());
        assertFalse(player.hasWon());
    }

    @Test
    void testMove() {
        player.move(15, 20);
        assertEquals(15, player.getX());
        assertEquals(20, player.getY());
    }

    @Test
    void testAddAndGetCard() {
        assertTrue(player.getCards().isEmpty());

        player.addCard(card);

        assertEquals(1, player.getCards().size());
        assertTrue(player.getCards().contains(card));
    }

    @Test
    void testGetCardsReturnsDefensiveCopy() {
        player.addCard(card);

        player.getCards().clear(); // This should not affect the original list

        assertEquals(0, player.getCards().size());
    }

    @Test
    void testSetCurrentPlayer() {
        assertFalse(player.isCurrentPlayer());

        player.setCurrentPlayer(true);
        assertTrue(player.isCurrentPlayer());

        player.setCurrentPlayer(false);
        assertFalse(player.isCurrentPlayer());
    }

    @Test
    void testSetActive() {
        assertTrue(player.isActive());

        player.setActive(false);
        assertFalse(player.isActive());

        player.setActive(true);
        assertTrue(player.isActive());
    }

    @Test
    void testSetHasWon() {
        assertFalse(player.hasWon());

        player.setHasWon(true);
        assertTrue(player.hasWon());

        player.setHasWon(false);
        assertFalse(player.hasWon());
    }

}
