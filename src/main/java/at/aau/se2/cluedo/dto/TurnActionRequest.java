package at.aau.se2.cluedo.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnActionRequest {
    private String playerName;
    private String actionType; // "DICE_ROLL", "MOVEMENT", "SUGGESTION", "ACCUSATION"
    private int diceValue;
    private String suspect;
    private String weapon;
    private String room;
}
