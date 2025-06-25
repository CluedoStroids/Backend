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
        if (report == null ||
                report.getLobbyId() == null ||
                report.getAccuser() == null ||
                report.getSuspect() == null ||
                report.getAccuser().equals(report.getSuspect())) {
            logger.warn("Invalid cheating report received: {}", report);
            return;
        }

        GameManager game = gameService.getGame(report.getLobbyId());
        if (game == null) return;

        Player accuser = game.getPlayer(report.getAccuser());
        Player suspect = game.getPlayer(report.getSuspect());
        if (accuser == null || suspect == null) return;
        if (!accuser.isCanReport()) return;

        if (!game.inRoom(accuser)) {
            game.resetPlayer(accuser);
            accuser.setCanReport(false);
            messagingTemplate.convertAndSend(
                    "/topic/playerReset/" + report.getLobbyId(),
                    Map.of("player", accuser.getName(), "x", accuser.getX(), "y", accuser.getY())
            );
            messagingTemplate.convertAndSend(
                    "/topic/cheating/" + report.getLobbyId(),
                    Map.of(
                            "type", "CHEATING_REPORT",
                            "suspect", suspect.getName(),
                            "accuser", accuser.getName(),
                            "valid", false,
                            "reason", "NOT_IN_ROOM"
                    )
            );
            return;
        }

        String currentRoom = game.getCurrentRoom(suspect);
        boolean isCheating = currentRoom != null && game.getSuggestionCount(suspect, currentRoom) > 1;


        if (isCheating) {
            game.resetPlayer(suspect);
            messagingTemplate.convertAndSend(
                    "/topic/playerReset/" + report.getLobbyId(),
                    Map.of("player", suspect.getName(), "x", suspect.getX(), "y", suspect.getY())
            );
        } else {
            game.resetPlayer(accuser);
            accuser.setCanReport(false);
            messagingTemplate.convertAndSend(
                    "/topic/playerReset/" + report.getLobbyId(),
                    Map.of("player", accuser.getName(), "x", accuser.getX(), "y", accuser.getY())
            );
        }

        messagingTemplate.convertAndSend(
                "/topic/cheating/" + report.getLobbyId(),
                Map.of(
                        "type", "CHEATING_REPORT",
                        "suspect", suspect.getName(),
                        "accuser", accuser.getName(),
                        "valid", isCheating,
                        "reason", "SUCCESS"
                )
        );
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
}



