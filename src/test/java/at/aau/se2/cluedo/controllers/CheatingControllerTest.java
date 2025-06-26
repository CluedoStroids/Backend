package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.CheatingReport;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.services.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;

import java.util.Map;

import static org.mockito.Mockito.*;

class CheatingControllerTest {

    private GameService gameService;
    private SimpMessagingTemplate messagingTemplate;
    private CheatingController cheatingController;
    private GameManager mockGameManager;
    private Player accuserPlayer;
    private Player suspectPlayer;
    private CheatingReport report;

    @BeforeEach
    void setUp() {
        gameService = mock(GameService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        cheatingController = new CheatingController(gameService, messagingTemplate);
        mockGameManager = mock(GameManager.class);
        accuserPlayer = mock(Player.class);
        suspectPlayer = mock(Player.class);
        report = new CheatingReport();
        report.setLobbyId("2131230973");
        report.setAccuser("Accuser");
        report.setSuspect("Suspect");
    }

    @Test
    void testHandleCheatingReport_sendsMessageAndRecordsReport() {
        CheatingReport report = new CheatingReport();
        report.setLobbyId("2131230973");
        report.setAccuser("Professor Plum");
        report.setSuspect("Colonel Mustard");

        when(gameService.getGame("2131230973")).thenReturn(mockGameManager);
        when(mockGameManager.getPlayer("Professor Plum")).thenReturn(accuserPlayer);
        when(mockGameManager.getPlayer("Colonel Mustard")).thenReturn(suspectPlayer);

        when(accuserPlayer.isCanReport()).thenReturn(true);
        when(mockGameManager.inRoom(accuserPlayer)).thenReturn(true);

        when(accuserPlayer.getName()).thenReturn("Professor Plum");
        when(suspectPlayer.getName()).thenReturn("Colonel Mustard");

        when(mockGameManager.getLastSuggestion("Colonel Mustard"))
                .thenReturn(new GameManager.SuggestionRecord("Colonel Mustard", "Kitchen", "Knife"));
        when(mockGameManager.getCurrentRoom(accuserPlayer)).thenReturn("Kitchen");
        when(mockGameManager.getCurrentRoom(suspectPlayer)).thenReturn("Kitchen");

        when(suspectPlayer.getSuggestionsInCurrentRoom()).thenReturn(2);

        cheatingController.handleCheatingReport(report);

        verify(messagingTemplate).convertAndSend(
                ("/topic/cheating/2131230973"),
                Map.of(
                        "type", "CHEATING_REPORT",
                        "suspect", "Colonel Mustard",
                        "accuser", "Professor Plum",
                        "valid", true,
                        "reason", "SUCCESS"
                )
        );
    }


    @Test
    void testHandleCheatingReport_invalidReport_doesNothing() {
        CheatingReport report = new CheatingReport();

        cheatingController.handleCheatingReport(report);

        verifyNoInteractions(gameService);
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void testHandleCheatingReport_gameNotFound_doesNothing() {
        CheatingReport report = new CheatingReport();
        report.setLobbyId("2131230973");
        report.setAccuser("Plum");
        report.setSuspect("Mustard");

        when(gameService.getGame("2131230973")).thenReturn(null);

        cheatingController.handleCheatingReport(report);

        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void testValidCheatingReport_ResetsSuspect() {
        when(gameService.getGame("2131230973")).thenReturn(mockGameManager);
        when(mockGameManager.getPlayer("Accuser")).thenReturn(accuserPlayer);
        when(mockGameManager.getPlayer("Suspect")).thenReturn(suspectPlayer);

        when(accuserPlayer.isCanReport()).thenReturn(true);
        when(mockGameManager.inRoom(accuserPlayer)).thenReturn(true);
        when(mockGameManager.getCurrentRoom(accuserPlayer)).thenReturn("Kitchen");
        when(mockGameManager.getCurrentRoom(suspectPlayer)).thenReturn("Kitchen");

        when(accuserPlayer.getName()).thenReturn("Accuser");
        when(suspectPlayer.getName()).thenReturn("Suspect");

        when(mockGameManager.getLastSuggestion("Suspect"))
                .thenReturn(new GameManager.SuggestionRecord("Suspect", "Kitchen", "Knife"));

        when(suspectPlayer.getSuggestionsInCurrentRoom()).thenReturn(2);
        when(mockGameManager.hasPlayerLeftRoom(suspectPlayer, "Kitchen")).thenReturn(false);

        cheatingController.handleCheatingReport(report);

        verify(mockGameManager).resetPlayer(argThat(player ->
                player != null && "Suspect".equals(player.getName())
        ));

        verify(messagingTemplate).convertAndSend(
                startsWith("/topic/playerReset/"),
                any(Object.class)
        );
    }

    @Test
    void testFalseCheatingReport_ResetsAccuser() {
        when(gameService.getGame("2131230973")).thenReturn(mockGameManager);
        when(mockGameManager.getPlayer("Accuser")).thenReturn(accuserPlayer);
        when(mockGameManager.getPlayer("Suspect")).thenReturn(suspectPlayer);

        when(accuserPlayer.isCanReport()).thenReturn(true);
        when(mockGameManager.inRoom(accuserPlayer)).thenReturn(true);

        when(accuserPlayer.getName()).thenReturn("Accuser");
        when(accuserPlayer.getX()).thenReturn(5);
        when(accuserPlayer.getY()).thenReturn(6);
        when(suspectPlayer.getName()).thenReturn("Suspect");

        when(mockGameManager.getCurrentRoom(accuserPlayer)).thenReturn("Kitchen");
        when(mockGameManager.getCurrentRoom(suspectPlayer)).thenReturn("Kitchen");
        when(suspectPlayer.getSuggestionsInCurrentRoom()).thenReturn(1);

        cheatingController.handleCheatingReport(report);

        verify(mockGameManager).resetPlayer(accuserPlayer);
        verify(accuserPlayer).setCanReport(false);

        verify(messagingTemplate).convertAndSend(
                "/topic/playerReset/2131230973",
                Map.of("player", "Accuser", "x", 5, "y", 6)
        );
    }


    @Test
    void testCheatingReport_NotSameRoom_ResetsAccuser() {
        when(gameService.getGame("2131230973")).thenReturn(mockGameManager);
        when(mockGameManager.getPlayer("Accuser")).thenReturn(accuserPlayer);
        when(mockGameManager.getPlayer("Suspect")).thenReturn(suspectPlayer);

        when(accuserPlayer.isCanReport()).thenReturn(true);
        when(mockGameManager.inRoom(accuserPlayer)).thenReturn(true);
        when(mockGameManager.getCurrentRoom(accuserPlayer)).thenReturn("Kitchen");
        when(mockGameManager.getCurrentRoom(suspectPlayer)).thenReturn("Library");

        when(accuserPlayer.getName()).thenReturn("Accuser");
        when(accuserPlayer.getX()).thenReturn(2);
        when(accuserPlayer.getY()).thenReturn(3);
        when(suspectPlayer.getName()).thenReturn("Suspect");

        cheatingController.handleCheatingReport(report);

        verify(mockGameManager).resetPlayer(accuserPlayer);
        verify(accuserPlayer).setCanReport(false);

        verify(messagingTemplate).convertAndSend(
                "/topic/playerReset/2131230973",
                Map.of("player", "Accuser", "x", 2, "y", 3)
        );

        verify(messagingTemplate).convertAndSend(
                "/topic/cheating/2131230973",
                Map.of(
                        "type", "CHEATING_REPORT",
                        "suspect", "Suspect",
                        "accuser", "Accuser",
                        "valid", false,
                        "reason", "NOT_IN_SAME_ROOM"
                )
        );
    }

}
