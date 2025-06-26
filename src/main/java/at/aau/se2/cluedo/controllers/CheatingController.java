package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.CheatingReport;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.services.GameService;
import at.aau.se2.cluedo.models.gameobjects.Player;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        Set<String> alreadyAccused = game.getCheatingReports()
                .getOrDefault(report.getAccuser(), new HashSet<>());

        if (alreadyAccused.contains(report.getSuspect())) {            return;
        }

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
            game.getCheatingReports().computeIfAbsent(report.getAccuser(), k -> new HashSet<>()).add(report.getSuspect());
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

        String accuserRoom = game.getCurrentRoom(accuser);
        String suspectRoom = game.getCurrentRoom(suspect);

        if (accuserRoom == null || suspectRoom == null || !accuserRoom.equals(suspectRoom)) {
            game.resetPlayer(accuser);
            accuser.setCanReport(false);
            messagingTemplate.convertAndSend(
                    "/topic/playerReset/" + report.getLobbyId(),
                    Map.of("player", accuser.getName(), "x", accuser.getX(), "y", accuser.getY())
            );
            game.getCheatingReports().computeIfAbsent(report.getAccuser(), k -> new HashSet<>()).add(report.getSuspect());
            messagingTemplate.convertAndSend(
                    "/topic/cheating/" + report.getLobbyId(),
                    Map.of(
                            "type", "CHEATING_REPORT",
                            "suspect", suspect.getName(),
                            "accuser", accuser.getName(),
                            "valid", false,
                            "reason", "NOT_IN_SAME_ROOM"
                    )
            );
            return;
        }


        boolean isCheating = suspect.getSuggestionsInCurrentRoom() > 1;


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
        game.getCheatingReports().computeIfAbsent(report.getAccuser(), k -> new HashSet<>()).add(report.getSuspect());
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
    }


