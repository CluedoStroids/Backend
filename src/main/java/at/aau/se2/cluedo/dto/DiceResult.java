package at.aau.se2.cluedo.dto;

public class DiceResult {
    private int diceOne;
    private int diceTwo;

    public DiceResult(int diceOne, int diceTwo) {
        this.diceOne = diceOne;
        this.diceTwo = diceTwo;
    }

    public int getDiceOne() {
        return diceOne;
    }

    public int getDiceTwo() {
        return diceTwo;
    }
}
