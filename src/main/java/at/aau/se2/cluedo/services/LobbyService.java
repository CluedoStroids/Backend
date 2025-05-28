package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.models.gameobjects.Player;
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

    public List<Lobby> getAllActiveLobbies() {
        return lobbyRegistry.getAllLobbies();
    }
}
