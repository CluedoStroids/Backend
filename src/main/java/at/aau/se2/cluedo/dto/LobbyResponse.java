package at.aau.se2.cluedo.dto;

import at.aau.se2.cluedo.models.Lobby;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LobbyResponse {
    private String id;
    private String host;
    private List<String> participants;


    public static LobbyResponse fromLobby(Lobby lobby) {
        return new LobbyResponse(
                lobby.getId(),
                lobby.getHost(),
                lobby.getParticipants()
        );
    }
}
