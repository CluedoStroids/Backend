package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.models.Lobby;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class LobbyService {
    private Map<String, Lobby> lobbies = new HashMap<>();

    public String createLobby(String host) {
        String lobbyId = UUID.randomUUID().toString();
        Lobby lobby = new Lobby(lobbyId, host);
        lobbies.put(lobbyId, lobby);
        System.out.println("Created lobby: " + lobbyId);
        return lobbyId;
    }

    public void joinLobby(String lobbyId, String user) {
        Lobby lobby = lobbies.get(lobbyId);
        if (lobby == null) {
            throw new RuntimeException("Lobby not found");
        }
        if (!lobby.getParticipants().contains(user)) {
            lobby.getParticipants().add(user);
            System.out.println("User: " + user + " joined lobby " + lobbyId);
        }
    }

    public void leaveLobby(String lobbyId, String user) {
        Lobby lobby = lobbies.get(lobbyId);
        if (lobby == null) {
            throw new RuntimeException("Lobby not found");
        }
        if (!lobby.getParticipants().contains(user)) {
            lobby.getParticipants().remove(user);
            System.out.println("User: " + user + " left lobby " + lobbyId);
        }
    }

    public Lobby getLobby(String lobbyId) {
        Lobby lobby = lobbies.get(lobbyId);
        if (lobby == null) {
            throw new RuntimeException("Lobby not found");
        }
        return lobby;
    }
}