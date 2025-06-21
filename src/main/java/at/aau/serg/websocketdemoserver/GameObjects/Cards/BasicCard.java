package at.aau.serg.websocketdemoserver.GameObjects.Cards;

import at.aau.serg.websocketdemoserver.GameObjects.GameObject;
import lombok.Getter;

import java.util.UUID;
//Make Enum for the Card Type
@Getter
public class BasicCard extends GameObject {

    //Variables
    private final String cardName;
    private final UUID cardID;
    private final String cardValue;
    private final String type;


    public BasicCard(String cardName, UUID cardID, String cardValue, String type) {
        this.cardName = cardName;
        this.cardID = cardID;
        this.cardValue = cardValue;
        this.type = type;
    }

}
