package at.aau.serg.websocketdemoserver.GameManager;

import at.aau.serg.websocketdemoserver.GameObjects.Cards.BasicCard;
import at.aau.serg.websocketdemoserver.GameObjects.GameObject;
import at.aau.serg.websocketdemoserver.GameObjects.Player;
import at.aau.serg.websocketdemoserver.GameObjects.SecretFile;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class GameManager
{   //List that save the Player stats and that save the cards
    private ArrayList<Player> playerList = new ArrayList<>();
    private int currentPlayer=0;
    private ArrayList<BasicCard> cards = new ArrayList<>();
    private ArrayList<BasicCard> rooms = new ArrayList<>();
    private ArrayList<BasicCard> weapons = new ArrayList<>();
    private  ArrayList<BasicCard> character = new ArrayList<>();

    private SecretFile secretFile;

// Add a Player to the List
    public void addPlayer(Player p){
        playerList.add(p);
    }
    //get A certain Player
    public Player getPlayer(int p){
        return playerList.get(p);
    }
    //get the Current Player
    public Player getCurrentPlayer(){
        return playerList.get(currentPlayer);
    }
    //Next Turn in the Game
    public void nextTurn(){
        currentPlayer+=1;
        if(currentPlayer >= playerList.size()){
            topOfTheRound();
        }
    }

// Top of the Round so restarting at 0
    public void topOfTheRound(){
        currentPlayer = 0;
    }
// Init the Game
    public void InitilizeGame(){
            //Call GameBoard
            generateFile();
    }
//Generate the secret File.
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
//Solve the case
    public boolean solveFile(SecretFile file){
        if (secretFile.equals(file)){
            return true;
        }
        return false;
    }
    //get a Card


    public GameObject getACard(){
        return new BasicCard("Knife", UUID.randomUUID(),"It's a Knife","Weapon");
    }
//Dice
    public int randomDice(){
        Random rn = new Random();
        return rn.nextInt(12)+1;
    }


}
