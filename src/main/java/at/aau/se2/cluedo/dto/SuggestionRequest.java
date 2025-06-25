package at.aau.se2.cluedo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionRequest {
    private String playerName;
    private String playerId;
    private String lobbyId;
    private String suspect;
    private String weapon;
    private String room;

    public SuggestionRequest(String playerName, String playerId, String suspect, String weapon, String room) {
        this.playerName = playerName;
        this.playerId = playerId;
        this.suspect = suspect;
        this.weapon = weapon;
        this.room = room;
    }
}
