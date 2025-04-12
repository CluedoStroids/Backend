package at.aau.se2.cluedo.models.cards;

import at.aau.se2.cluedo.models.gameobjects.GameObject;
import lombok.Getter;

import java.util.UUID;
//Make Enum for the Card Type
@Getter
public class BasicCard extends GameObject {

    //Variables
    private final String cardName;
    private final UUID cardID;
    private final String cardValue; //IDK yet
    private final String type;


    public BasicCard(String cardName, UUID cardID, String cardValue, String type) {
        this.cardName = cardName;
        this.cardID = cardID;
        this.cardValue = cardValue;
        this.type = type;
    }

    public boolean equals(BasicCard card){
        return (cardName == card.cardName);
    }

}
