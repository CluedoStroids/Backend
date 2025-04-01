package at.aau.serg.websocketdemoserver.GameObjects.Cards;

import at.aau.serg.websocketdemoserver.GameObjects.GameObject;
import lombok.Getter;

import java.util.UUID;

@Getter
public class BasicCard extends GameObject {

    private final String cardName;
    private final UUID cardID;
    private final String CardValue;


    public BasicCard(String cardName, UUID cardID, String cardValue) {
        this.cardName = cardName;
        this.cardID = cardID;
        CardValue = cardValue;
    }

}
