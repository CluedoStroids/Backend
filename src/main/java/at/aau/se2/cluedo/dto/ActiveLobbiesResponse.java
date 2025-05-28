package at.aau.se2.cluedo.dto;

import at.aau.se2.cluedo.models.lobby.Lobby;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveLobbiesResponse {
    private List<LobbyInfo> lobbies;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LobbyInfo {
        private String id;
        private String hostName;
        private int playerCount;
    }

    public static ActiveLobbiesResponse fromLobbies(List<Lobby> lobbies) {
        List<LobbyInfo> lobbyInfos = lobbies.stream()
                .map(lobby -> new LobbyInfo(
                        lobby.getId(),
                        lobby.getHost().getName(),
                        lobby.getPlayers().size()))
                .toList();
        
        return new ActiveLobbiesResponse(lobbyInfos);
    }
}
