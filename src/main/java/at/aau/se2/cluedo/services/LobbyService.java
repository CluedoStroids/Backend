package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.models.lobby.Lobby;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import at.aau.se2.cluedo.dto.SolveCaseRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
public class LobbyService {

    private final LobbyRegistry lobbyRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public LobbyService(LobbyRegistry lobbyRegistry, SimpMessagingTemplate messagingTemplate) {
        this.lobbyRegistry = lobbyRegistry;
        this.messagingTemplate = messagingTemplate;
    }

    public String createLobby(String host) {
        Lobby lobby = lobbyRegistry.createLobby(host);
        lobby.setGameManager(new GameManager());
        return lobby.getId();
    }

    public void joinLobby(String lobbyId, String user) {
        Lobby lobby = lobbyRegistry.getLobby(lobbyId);
        lobby.addParticipant(user);
    }

    public void leaveLobby(String lobbyId, String user) {
        Lobby lobby = lobbyRegistry.getLobby(lobbyId);
        lobby.removeParticipant(user);
    }

    public Lobby getLobby(String lobbyId) {
        return lobbyRegistry.getLobby(lobbyId);
    }

    public void solveCase(SolveCaseRequest request) {
        Lobby lobby = lobbyRegistry.getLobby(request.getLobbyId());
        if (lobby == null) {
            return;
        }

        GameManager gameManager = lobby.getGameManager();
        if (gameManager == null) {
            return;
        }

        if (!gameManager.getCurrentPlayer().isActive()) {
            messagingTemplate.convertAndSend("/topic/lobby/" + request.getLobbyId(), "already_eliminated");
            return;
        }

        String correctSuspect = gameManager.getCorrectSuspect();
        String correctRoom = gameManager.getCorrectRoom();
        String correctWeapon = gameManager.getCorrectWeapon();

        boolean isCorrect = correctSuspect.equals(request.getSuspect()) &&
                correctRoom.equals(request.getRoom()) &&
                correctWeapon.equals(request.getWeapon());

        if (isCorrect) {
            messagingTemplate.convertAndSend("/topic/lobby/" + request.getLobbyId(), "correct");
        } else {
            gameManager.eliminateCurrentPlayer();
            messagingTemplate.convertAndSend("/topic/lobby/" + request.getLobbyId(), "wrong");
            messagingTemplate.convertAndSend("/topic/lobby/" + request.getLobbyId() + "/players", gameManager.getPlayerList());
        }
    }
}
