package GameBoard;

import at.aau.serg.websocketdemoserver.GameObjects.Cards.BasicCard;
import at.aau.serg.websocketdemoserver.GameObjects.GameObject;

import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

public class Player extends GameObject {
    private final String name;
    private final String character;
    private int x;
    private int y;
    private final List<BasicCard> cards;
    private boolean isCurrentPlayer;
    private boolean isActive = true;
    private boolean hasWon = false;

      public UUID getPlayerID() {
        return playerID;
    }

    // Player UUID for Unique identification
    private UUID playerID;

    public Player(String name, String character, int startX, int startY) {
        this.name = name;
        this.playerID = UUID.randomUUID();
        this.character = character;
        this.x = startX;
        this.y = startY;
        this.cards = new ArrayList<>();
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

    // Getters and Setters
    public String getName() { return name; }
    public String getCharacter() { return character; }
    public int getX() { return x; }
    public int getY() { return y; }
    public List<BasicCard> getCards() { return new ArrayList<>(cards); }
    public boolean isCurrentPlayer() { return isCurrentPlayer; }
    public void setCurrentPlayer(boolean isCurrentPlayer) { this.isCurrentPlayer = isCurrentPlayer; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
    public boolean hasWon() { return hasWon; }
    public void setHasWon(boolean hasWon) { this.hasWon = hasWon; }
}
