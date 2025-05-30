package at.aau.se2.cluedo.dto;

import at.aau.se2.cluedo.services.TurnService.TurnState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnStateResponse {
    private String lobbyId;
    private String currentPlayerName;
    private int currentPlayerIndex;
    private TurnState turnState;
    private boolean canMakeSuggestion;
    private boolean canMakeAccusation;
    private int diceValue;
    private String message;
}
