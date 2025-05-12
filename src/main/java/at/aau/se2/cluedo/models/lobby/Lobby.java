package at.aau.se2.cluedo.models.lobby;

import at.aau.se2.cluedo.models.gameobjects.Player;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter

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


    public boolean removePlayer(Player player) {
        boolean removed = players.removeIf(p -> p.getPlayerID().equals(player.getPlayerID()));
        if (removed) {
            logger.info("Player: {} left lobby {}", player.getName(), id);
            return true;
        } else {
            logger.debug("Player: {} not found in lobby {}", player.getName(), id);
            return false;
        }
    }


    public boolean hasPlayer(Player player) {
        return players.stream().anyMatch(p -> p.getPlayerID().equals(player.getPlayerID()));
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