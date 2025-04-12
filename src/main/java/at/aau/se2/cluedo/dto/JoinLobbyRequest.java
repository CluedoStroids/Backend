package at.aau.se2.cluedo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinLobbyRequest {

    @NotBlank(message = "Username cannot be empty")
    private String username;
}
