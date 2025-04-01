package at.aau.serg.websocketdemoserver.GameManager;

import at.aau.serg.websocketdemoserver.GameObjects.Cards.BasicCard;
import at.aau.serg.websocketdemoserver.GameObjects.GameObject;
import at.aau.serg.websocketdemoserver.GameObjects.Player;
import at.aau.serg.websocketdemoserver.GameObjects.SecretFile;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class GameManager
{
    private ArrayList<Player> playerList = new ArrayList<>();
    private int currentPlayer=0;
    private ArrayList<BasicCard> cards = new ArrayList<>();
    private ArrayList<BasicCard> rooms = new ArrayList<>();
    private ArrayList<BasicCard> weapons = new ArrayList<>();
    private  ArrayList<BasicCard> character = new ArrayList<>();

    private SecretFile secretFile;


    public void addPlayer(Player p){
        playerList.add(p);
    }
    public Player getPlayer(int p){
        return playerList.get(p);
    }
    public Player getCurrentPlayer(){
        return playerList.get(currentPlayer);
    }
    public void nextTurn(){
        currentPlayer+=1;
        if(currentPlayer >= playerList.size()){
            topOfTheRound();
        }
    }


    public void topOfTheRound(){
        currentPlayer = 0;
    }

    public void InitilizeGame(){
            //Call GameBoard
            generateFile();
    }

    public void generateFile(){
        cards.clear();
        //pick room
        Random rn = new Random();
        int room = rn.nextInt(rooms.size());
        //pick char
        int chara = rn.nextInt(character.size());
        // pick weapon
        int weapon = rn.nextInt(weapons.size());

        secretFile = new SecretFile(rooms.remove(room),weapons.remove(weapon),character.remove(chara));
        cards.addAll(rooms);
        cards.addAll(weapons);
        cards.addAll(character);
    }

    public boolean solveFile(SecretFile file){
        if (secretFile.equals(file)){
            return true;
        }
        return false;
    }
    public GameObject getCard(GameObject card){
        return null;
    }

    public GameObject getACard(){
        return new BasicCard("Knife", UUID.randomUUID(),"It's a Knife","Weapon");
    }

    public int randomDice(){
        Random rn = new Random();
        return rn.nextInt(12)+1;
    }


}
