package at.aau.se2.cluedo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccusationRequest {
    private String lobbyId;
    private String username;
    private String suspect;
    private String room;
    private String weapon;
}
