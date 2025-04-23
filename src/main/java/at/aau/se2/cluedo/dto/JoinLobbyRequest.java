package at.aau.se2.cluedo.dto;

import at.aau.se2.cluedo.models.gameobjects.Player;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinLobbyRequest {

    @NotNull(message = "Player information cannot be null")
    private Player player;
}
