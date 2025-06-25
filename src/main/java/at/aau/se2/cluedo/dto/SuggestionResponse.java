package at.aau.se2.cluedo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionResponse {

    private String lobbyId;
    private String playerId;
    private String cardName;


}
