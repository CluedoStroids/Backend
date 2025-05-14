package at.aau.se2.cluedo.models;

public class Random {
    public static int rand(int max, int min){
        long time = System.currentTimeMillis();
        return (int)((time % max) + min);
    }
}
