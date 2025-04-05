package GameBoard;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private final String character;
    private int x;
    private int y;
    private final List<String> cards;
    private boolean isCurrentPlayer;
    private boolean isActive = true;
    private boolean hasWon = false;

    public Player(String name, String character, int startX, int startY) {
        this.name = name;
        this.character = character;
        this.x = startX;
        this.y = startY;
        this.cards = new ArrayList<>();
    }

    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void addCard(String card) {
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
    public List<String> getCards() { return new ArrayList<>(cards); }
    public boolean isCurrentPlayer() { return isCurrentPlayer; }
    public void setCurrentPlayer(boolean isCurrentPlayer) { this.isCurrentPlayer = isCurrentPlayer; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
    public boolean hasWon() { return hasWon; }
    public void setHasWon(boolean hasWon) { this.hasWon = hasWon; }
}