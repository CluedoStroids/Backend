package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.models.gameboard.GameBoard;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import at.aau.se2.cluedo.models.lobby.Lobby;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LobbyService {



    private final LobbyRegistry lobbyRegistry;

    @Autowired
    public LobbyService(LobbyRegistry lobbyRegistry) {
        this.lobbyRegistry = lobbyRegistry;
    }

    @Setter
    public GameService gameService;

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

        return "No one could disprove your suggestion!";

    }

    public String makeAccusation(Player player, SecretFile acusation) {

        return "Wrong! " + player.getName() + " is out of the game!";
    }


    public void performMovement(Player player, List<String> movement, String lobbId) {

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
                default:
                    break;
            }

        }
        gameService.getGame(lobbId).getPlayer(player.getName()).move(x,y);
    }

    public List<Lobby> getAllActiveLobbies() {
        return lobbyRegistry.getAllLobbies();
    }
}
