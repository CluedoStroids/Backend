package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.CheatingReport;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.services.GameService;
import at.aau.se2.cluedo.models.gameobjects.Player;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class CheatingController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CheatingController.class);

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public CheatingController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/cheating")
    public void handleCheatingReport(CheatingReport report) {
        if (report.getLobbyId() == null || report.getSuspect() == null || report.getAccuser() == null) {
            logger.warn("Invalid cheating report received");
            return;
        }

        GameManager gameManager = gameService.getGame(report.getLobbyId());
        if (gameManager != null) {
            gameManager.reportCheating(report.getAccuser(), report.getSuspect());
            notifyPlayers(report);

            logger.info("Cheating report recorded for {}, total: {}",
                    report.getSuspect(), gameManager.getCheatingReportsCount(report.getSuspect()));
        }
    }

    @MessageMapping("/cheating/eliminate")
    public void manuallyEliminatePlayer(CheatingReport report) {
        GameManager gameManager = gameService.getGame(report.getLobbyId());
        if (gameManager != null) {
            eliminatePlayer(gameManager, report.getSuspect());
        }
    }

    private void eliminatePlayer(GameManager gameManager, String suspect) {
        Player player = gameManager.getPlayer(suspect);
        if (player != null && player.isActive()) {
            player.setActive(false);
            logger.info("Player {} has been eliminated for cheating", suspect);
            messagingTemplate.convertAndSend(
                    "/topic/elimination/" + gameManager.getLobbyId(),
                    Map.of("player", suspect, "reason", "CHEATING")
            );
        }
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



