package at.aau.serg.websocketdemoserver.GameObjects;

import java.util.UUID;

public class Player extends GameObject{

    public UUID getPlayerID() {
        return playerID;
    }

    // Player UUID for Unique identification
    private UUID playerID;

}
