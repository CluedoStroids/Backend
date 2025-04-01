package at.aau.serg.websocketdemoserver.GameManager;

import at.aau.serg.websocketdemoserver.GameObjects.Cards.BasicCard;
import at.aau.serg.websocketdemoserver.GameObjects.GameObject;

import java.util.Random;
import java.util.UUID;

public abstract class GameManager
{
    public void nextTurn(){

    }

    public void topOfTheRound(){

    }

    public void InitilizeGame(){

    }
    public GameObject getCard(GameObject card){
        return null;
    }

    public GameObject getACard(){
        return new BasicCard("Knife", UUID.randomUUID(),"It's a Knife");
    }

    public int randomDice(){
        Random rn = new Random();
        return rn.nextInt(12)+1;
    }


}
