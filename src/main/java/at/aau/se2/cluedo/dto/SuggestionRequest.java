package at.aau.se2.cluedo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionRequest {
    private String playerName;
    private String suspect;
    private String weapon;
    private String room; // Current room where suggestion is made
}
