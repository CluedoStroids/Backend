package at.aau.se2.cluedo.models.cards;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public class BasicCard{

    //Variables
    private final String cardName;
    private final UUID cardID;
    private final CardType type;

    public BasicCard(String cardName, UUID cardID, CardType type) {
        this.cardName = cardName;
        this.cardID = cardID;
        this.type = type;
    }


    public boolean cardEquals(BasicCard card){
        if(card == null){
            return false;
        }
        return (Objects.equals(cardName, card.cardName));
    }

    public static List<BasicCard> getRooms(){
        List<BasicCard> rooms = new ArrayList<>();
        rooms.add(new BasicCard("Kitchen", UUID.randomUUID(), CardType.ROOM));
        rooms.add(new BasicCard("Wintergarden", UUID.randomUUID(), CardType.ROOM));
        rooms.add(new BasicCard("Music room", UUID.randomUUID(), CardType.ROOM));
        rooms.add(new BasicCard("Billard room", UUID.randomUUID(), CardType.ROOM));
        rooms.add(new BasicCard("Dining room", UUID.randomUUID(), CardType.ROOM));
        rooms.add(new BasicCard("Hall", UUID.randomUUID(), CardType.ROOM));
        rooms.add(new BasicCard("Library", UUID.randomUUID(), CardType.ROOM));
        rooms.add(new BasicCard("Salon", UUID.randomUUID(), CardType.ROOM));
        rooms.add(new BasicCard("Office", UUID.randomUUID(), CardType.ROOM));
        return rooms;
    }

    public static List<BasicCard> getWeapons(){
        List<BasicCard> weapons = new ArrayList<>();
        weapons.add(new BasicCard("Wrench", UUID.randomUUID(), CardType.WEAPON));
        weapons.add(new BasicCard("Rope", UUID.randomUUID(), CardType.WEAPON));
        weapons.add(new BasicCard("Pipe", UUID.randomUUID(), CardType.WEAPON));
        weapons.add(new BasicCard("Pistol", UUID.randomUUID(), CardType.WEAPON));
        weapons.add(new BasicCard("Dagger", UUID.randomUUID(), CardType.WEAPON));
        weapons.add(new BasicCard("Candlestick", UUID.randomUUID(), CardType.WEAPON));
        return weapons;
    }

    public static List<BasicCard> getCharacters(){
        List<BasicCard> chracters = new ArrayList<>();
        chracters.add(new BasicCard("Miss Scarlet", UUID.randomUUID(), CardType.CHARACTER));
        chracters.add(new BasicCard("Colonel Mustard", UUID.randomUUID(), CardType.CHARACTER));
        chracters.add(new BasicCard("Mrs. White", UUID.randomUUID(), CardType.CHARACTER));
        chracters.add(new BasicCard("Mr. Green", UUID.randomUUID(), CardType.CHARACTER));
        chracters.add(new BasicCard("Mrs. Peacock", UUID.randomUUID(), CardType.CHARACTER));
        chracters.add(new BasicCard("Professor Plum", UUID.randomUUID(), CardType.CHARACTER));
        return chracters;
    }

}

