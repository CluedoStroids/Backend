package at.aau.se2.cluedo.models.gameobjects;

import at.aau.se2.cluedo.models.cards.BasicCard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player extends GameObject {
    private final String name;
    private final String character;
    private final List<BasicCard> cards;
    private int x;
    private int y;
    private boolean isCurrentPlayer;
    private boolean isActive = true;
    private boolean hasWon = false;
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

    public UUID getPlayerID() {
        return playerID;
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

    public String getName() {
        return name;
    }

    public String getCharacter() {
        return character;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public List<BasicCard> getCards() {
        return new ArrayList<>(cards);
    }

    public boolean isCurrentPlayer() {
        return isCurrentPlayer;
    }

    public void setCurrentPlayer(boolean isCurrentPlayer) {
        this.isCurrentPlayer = isCurrentPlayer;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean hasWon() {
        return hasWon;
    }

    public void setHasWon(boolean hasWon) {
        this.hasWon = hasWon;
    }

    public void setX(int i) {
    }
    public void setY(int i) {

    }


}