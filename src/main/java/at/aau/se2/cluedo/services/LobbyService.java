package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.models.lobby.Lobby;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class LobbyService {

    private final LobbyRegistry lobbyRegistry;

    @Autowired
    public LobbyService(LobbyRegistry lobbyRegistry) {
        this.lobbyRegistry = lobbyRegistry;
    }


    public String createLobby(String host) {
        Lobby lobby = lobbyRegistry.createLobby(host);
        return lobby.getId();
    }

    public void joinLobby(String lobbyId, String user) {
        Lobby lobby = lobbyRegistry.getLobby(lobbyId);
        lobby.addParticipant(user);
    }

    public void leaveLobby(String lobbyId, String user) {
        Lobby lobby = lobbyRegistry.getLobby(lobbyId);
        lobby.removeParticipant(user);
    }

    public Lobby getLobby(String lobbyId) {
        return lobbyRegistry.getLobby(lobbyId);
    }
}
