package at.aau.se2.cluedo.models;

import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.cards.CardType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class BasicCardTest {

    private BasicCard card1;
    private BasicCard card2;
    private BasicCard card3;
    private UUID uuid1;
    private UUID uuid2;

    @BeforeEach
    void setUp() {
        uuid1 = UUID.randomUUID();
        uuid2 = UUID.randomUUID();

        card1 = new BasicCard("Knife", uuid1, CardType.WEAPON);
        card2 = new BasicCard("Knife", uuid2,  CardType.WEAPON);
        card3 = new BasicCard("Rope", uuid2,  CardType.WEAPON);
    }

    @Test
    void testCardCreation() {
        assertEquals("Knife", card1.getCardName());
        assertEquals(uuid1, card1.getCardID());
        assertEquals(card3.getType(), card1.getType());
    }

    @Test
    void testCardCardEquals() {
        // Same name should be equal
        assertTrue(card1.cardEquals(card2));

        // Different name should not be equal
        assertFalse(card1.cardEquals(card3));
    }

    @Test
    void testCardNotEqualToNull() {
        assertFalse(card1.cardEquals(null));
    }

    @Test
    void testDifferentValuesButSameNameAreEqual() {
        BasicCard cardWithDifferentValue = new BasicCard("Knife", UUID.randomUUID(),  CardType.WEAPON);
        assertTrue(card1.cardEquals(cardWithDifferentValue));
    }
}
