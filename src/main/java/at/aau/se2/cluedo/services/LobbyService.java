package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.models.gameboard.GameBoard;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import at.aau.se2.cluedo.models.lobby.Lobby;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import at.aau.se2.cluedo.services.GameService;

import java.util.List;

@Service
public class LobbyService {



    private final LobbyRegistry lobbyRegistry;

    public LobbyRegistry getLobbyRegistry() {
        return lobbyRegistry;
    }

    @Autowired
    public LobbyService(LobbyRegistry lobbyRegistry) {
        this.lobbyRegistry = lobbyRegistry;
    }

    public GameService gameService;

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    public String createLobby(Player host) {
        Lobby lobby = lobbyRegistry.createLobby(host);
        return lobby.getId();
    }

    public void joinLobby(String lobbyId, Player player) {
        Lobby lobby = lobbyRegistry.getLobby(lobbyId);
        lobby.addPlayer(player);
    }

    public void leaveLobby(String lobbyId, Player player) {
        Lobby lobby = lobbyRegistry.getLobby(lobbyId);
        lobby.removePlayer(player);
    }

    public Lobby getLobby(String lobbyId) {
        return lobbyRegistry.getLobby(lobbyId);
    }

    public String makeSuggestion(Player player, String suspect, String weapon) {
/*
        String room = gameBoard.getCell(player.getX(), player.getY()).getRoom().getName();

        // Gather evidence
        for (Player p : this.players) {
            if (p != player) {
                for (BasicCard card : p.getCards()) {
                    if (card.equals(suspect) || card.equals(weapon) || card.equals(room)) {
                        System.out.println(p.getName() + " shows you: " + card);
                        return;
                    }
                }
            }
        }*/
        return "No one could disprove your suggestion!";

    }

    public String makeAccusation(Player player, SecretFile acusation) {
/*
        if (secretFile.room().equals(acusation.room()) && secretFile.character().equals(acusation.character()) && secretFile.weapon().equals(acusation.weapon())) {
            System.out.println("Correct! " + player.getName() + " has solved the crime!");
            player.setHasWon(true);
        } else {
            System.out.println("Wrong! " + player.getName() + " is out of the game!");
            player.setActive(false);
        }

 */
        return "Wrong! " + player.getName() + " is out of the game!";
    }


    public int performMovement(Player player, List<String> movement,String lobbId) {

        int x = gameService.getGame(lobbId).getPlayer(player.getName()).getX();
        int y = gameService.getGame(lobbId).getPlayer(player.getName()).getY();

        for (String move : movement) {
            switch (move) {
                case "W":
                    y--;
                    if (y > GameBoard.HEIGHT) {
                        y = GameBoard.HEIGHT;
                    }
                    break;
                case "D":
                    x++;
                    if (x > GameBoard.WIDTH) {
                        x = GameBoard.WIDTH;
                    }
                    break;
                case "A":
                    x--;
                    if (x < 0) {
                        x = 0;
                    }
                    break;
                case "S":
                    y++;
                    if (y < 0) {
                        y = 0;
                    }
                    break;
            }

        }
        gameService.getGame(lobbId).getPlayer(player.getName()).move(x,y);
/*
        if(movement.size() == 0){
            return 0;
        }

        if(movement.size() > diceRollS){
            return 0;
        }

        for (String input: movement) {

            if (input.equalsIgnoreCase("X")) {
                return 0;
            }

            int newX = player.getX();
            int newY = player.getY();

            switch (input.toUpperCase()) {
                case "W" -> newY--;
                case "S" -> newY++;
                case "A" -> newX--;
                case "D" -> newX++;
                default -> {
                    System.out.println("Invalid input!");
                    return 0;
                }
            }

            if (gameBoard.movePlayer(player, newX, newY)) {
                if(input != movement.getLast()){
                    movement.subList(1,movement.size()-1);
                    continue;
                }
                return performMovement(player, movement.subList(1,movement.size()-1));
            } else {
                System.out.println("Invalid move!");
                return performMovement(player,movement);
            }
        }
        System.out.println("Invalid move!");
        return performMovement(player, movement);


 */
        return 0;
    }

    public String displayGameBoard(List<Player> players) {


        /*
        System.out.println("\n=== CLUE GAME BOARD ===");


        System.out.print("  ");
        for (int i = 0; i < WIDTH; i++) {
            System.out.print(i % 10 + " ");

        }
        System.out.println();

        for (int y = 0; y < HEIGHT; y++) {
            System.out.print(y % 10 + " ");

            for (int x = 0; x < WIDTH; x++) {

                GameBoardCell cell = grid[x][y];
                char symbol = getSymbol(cell);
                String color = getColor(cell, players, x, y);

                System.out.print(color + symbol + RESET + " ");
            }

            System.out.println();
        }

        displayLegend(players.size());
        */
        return "[0,0,0,1,0,1,0";
    }

    public List<Lobby> getAllActiveLobbies() {
        return lobbyRegistry.getAllLobbies();
    }
}
