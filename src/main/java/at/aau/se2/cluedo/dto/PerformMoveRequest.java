package at.aau.se2.cluedo.dto;

import at.aau.se2.cluedo.models.gameobjects.Player;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformMoveRequest {
    @NotNull(message = "Player information cannot be null")
    private Player player;
    @NotNull(message = "moves information cannot be null")
    private ArrayList<String> moves;
}
