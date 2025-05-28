package at.aau.se2.cluedo.models;

// fixme use java's randoms
public class Random {
    private Random() {
    }

    public static int rand(int max, int min){
        long time = System.currentTimeMillis();
        return (int)((time % max) + min);
    }
}
