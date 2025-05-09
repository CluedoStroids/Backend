package at.aau.se2.cluedo.models.lobby;

import at.aau.se2.cluedo.models.gamemanager.GameManager;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Lobby {
    private static final Logger logger = LoggerFactory.getLogger(Lobby.class);

    private String id;
    private String host;
    private List<String> participants = new ArrayList<>();
    private transient GameManager gameManager;


    private String winnerUsername;

    public Lobby(String id, String host) {
        this.id = id;
        this.host = host;
        this.participants.add(host);
        logger.info("Created lobby: {} with host: {}", id, host);
    }

    public boolean addParticipant(String username) {
        if (!participants.contains(username)) {
            participants.add(username);
            logger.info("User: {} joined lobby {}", username, id);
            return true;
        } else {
            logger.debug("User: {} already in lobby {}", username, id);
            return false;
        }
    }

    public boolean removeParticipant(String username) {
        if (participants.contains(username)) {
            participants.remove(username);
            logger.info("User: {} left lobby {}", username, id);
            return true;
        } else {
            logger.debug("User: {} not found in lobby {}", username, id);
            return false;
        }
    }

    public boolean hasParticipant(String username) {
        return participants.contains(username);
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }


    public String getWinnerUsername() {
        return winnerUsername;
    }

    public void setWinnerUsername(String winnerUsername) {
        this.winnerUsername = winnerUsername;
    }
}
