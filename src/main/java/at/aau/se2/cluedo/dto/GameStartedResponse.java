package at.aau.se2.cluedo.dto;

import at.aau.se2.cluedo.models.gameobjects.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStartedResponse {
    private String lobbyId;
    private List<Player> players;
}
