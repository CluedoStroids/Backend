package at.aau.se2.cluedo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveLobbyRequest {

    @NotNull(message = "Player ID cannot be null")
    private UUID playerId;
}
