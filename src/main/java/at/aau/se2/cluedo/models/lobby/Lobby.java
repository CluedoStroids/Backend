package at.aau.se2.cluedo.models.lobby;

import at.aau.se2.cluedo.models.gameobjects.Player;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Data
@NoArgsConstructor
public class Lobby {
    private static final Logger logger = LoggerFactory.getLogger(Lobby.class);

    private String id;
    private UUID hostId;
    private List<Player> players = new ArrayList<>();


    public Lobby(String id, Player host) {
        this.id = id;
        this.hostId = host.getPlayerID();
        this.players.add(host);
        logger.info("Created lobby: {} with host: {}", id, host.getName());
    }


    public boolean addPlayer(Player player) {
        // Check if player with same ID already exists
        if (players.stream().noneMatch(p -> p.getPlayerID().equals(player.getPlayerID()))) {
            players.add(player);
            logger.info("Player: {} joined lobby {}", player.getName(), id);
            return true;
        } else {
            logger.debug("Player: {} already in lobby {}", player.getName(), id);
            return false;
        }
    }


    public boolean removePlayer(UUID playerId) {
        boolean removed = players.removeIf(p -> p.getPlayerID().equals(playerId));
        if (removed) {
            logger.info("Player with ID: {} left lobby {}", playerId, id);
            return true;
        } else {
            logger.debug("Player with ID: {} not found in lobby {}", playerId, id);
            return false;
        }
    }


    public boolean hasPlayer(UUID playerId) {
        return players.stream().anyMatch(p -> p.getPlayerID().equals(playerId));
    }

    public Player getHost() {
        return players.stream()
                .filter(p -> p.getPlayerID().equals(hostId))
                .findFirst()
                .orElse(null);
    }

    public List<String> getParticipantNames() {
        return players.stream()
                .map(Player::getName)
                .toList();
    }
}