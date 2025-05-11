package at.aau.se2.cluedo.models.gameobjects;

import at.aau.se2.cluedo.models.cards.BasicCard;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
public class Player extends GameObject {
    private final String name;
    private final String character;
    private final List<BasicCard> cards;
    private int x;
    private int y;
    private boolean isCurrentPlayer;
    private boolean isActive = true;

    public boolean hasWon() {
        return hasWon;
    }

    private boolean hasWon = false;
    // Player UUID for Unique identification
    private UUID playerID;
    private PlayerColor color;


    public Player(String name, String character, int startX, int startY, PlayerColor color) {
        this.name = name;
        this.playerID = UUID.randomUUID();
        this.character = character;
        this.x = startX;
        this.y = startY;
        this.cards = new ArrayList<>();
        this.color = color;
    }

    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void addCard(BasicCard card) {
        cards.add(card);
    }

    public boolean hasCard(String card) {
        return cards.contains(card);
    }
}
