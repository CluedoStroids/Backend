package at.aau.se2.cluedo.dto;

import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.lobby.Lobby;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LobbyResponse {
    private String id;
    private Player host;
    private List<Player> players;

    public static LobbyResponse fromLobby(Lobby lobby) {
        return new LobbyResponse(
                lobby.getId(),
                lobby.getHost(),
                lobby.getPlayers()
        );
    }
}
