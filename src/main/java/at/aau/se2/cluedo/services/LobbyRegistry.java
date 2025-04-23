package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.lobby.Lobby;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
public class LobbyRegistry {
    private static final Logger logger = LoggerFactory.getLogger(LobbyRegistry.class);

    private final Map<String, Lobby> lobbies = new HashMap<>();


    public Lobby createLobby(Player host) {
        String lobbyId = UUID.randomUUID().toString();
        Lobby lobby = new Lobby(lobbyId, host);
        lobbies.put(lobbyId, lobby);
        return lobby;
    }


    public Lobby getLobby(String lobbyId) {
        Lobby lobby = lobbies.get(lobbyId);
        if (lobby == null) {
            logger.error("Failed to get lobby: Lobby {} not found", lobbyId);
        }
        logger.debug("Retrieved lobby: {}", lobbyId);
        return lobby;
    }


    public boolean removeLobby(String lobbyId) {
        Lobby removed = lobbies.remove(lobbyId);
        if (removed != null) {
            logger.info("Removed lobby: {}", lobbyId);
            return true;
        }
        logger.debug("Failed to remove lobby: Lobby {} not found", lobbyId);
        return false;
    }
}
