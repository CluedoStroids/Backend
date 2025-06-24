package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.CheatingReport;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class CheatingController {

    private final Map<String, GameManager> gameManagers;
    private final SimpMessagingTemplate messagingTemplate;

    public CheatingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.gameManagers = new ConcurrentHashMap<>();
    }

    @MessageMapping("/cheating")
    public void handleCheatingReport(CheatingReport report) {
        GameManager gameManager = gameManagers.get(report.getLobbyId());
        if (gameManager != null) {
            gameManager.reportCheating(report.getAccuser(), report.getSuspect());
            notifyPlayers(report);
        }
    }

    public void registerGameManager(String lobbyId, GameManager gameManager) {
        gameManagers.put(lobbyId, gameManager);
    }

    private void notifyPlayers(CheatingReport report) {
        messagingTemplate.convertAndSend(
                "/topic/cheating/" + report.getLobbyId(),
                Map.of(
                        "type", "CHEATING_REPORT",
                        "suspect", report.getSuspect(),
                        "accuser", report.getAccuser()
                )
        );
    }
}
