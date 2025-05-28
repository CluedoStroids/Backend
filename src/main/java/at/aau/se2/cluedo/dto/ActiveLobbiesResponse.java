package at.aau.se2.cluedo.dto;

import at.aau.se2.cluedo.models.lobby.Lobby;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

// fixme if there are multiple lobbies why are some requests are without lobbyId?
// fixme superclasses for requests / responses with unified names (eg, id -> lobby id)
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
                .collect(Collectors.toList());
        
        return new ActiveLobbiesResponse(lobbyInfos);
    }
}
