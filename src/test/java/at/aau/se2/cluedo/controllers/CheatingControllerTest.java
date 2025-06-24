package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.CheatingReport;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.services.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;
import java.util.Optional;

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
        when(mockGameManager.getCheatingReportsCount("Colonel Mustard")).thenReturn(1);

        cheatingController.handleCheatingReport(report);

        verify(mockGameManager).reportCheating("Professor Plum", "Colonel Mustard");
        verify(messagingTemplate).convertAndSend(
                ("/topic/cheating/2131230973"),
                (Map.of(
                        "type", "CHEATING_REPORT",
                        "suspect", "Colonel Mustard",
                        "accuser", "Professor Plum"
                ))
        );
    }


    @Test
    void testManuallyEliminatePlayer_sendsEliminationMessage_withNumericLobbyId() {
        CheatingReport report = new CheatingReport();
        report.setLobbyId("2131230973");
        report.setSuspect("Red");

        Player mockPlayer = mock(Player.class);
        when(mockPlayer.isActive()).thenReturn(true);

        when(gameService.getGame("2131230973")).thenReturn(mockGameManager);
        when(mockGameManager.getPlayer("Red")).thenReturn(mockPlayer);
        when(mockGameManager.getLobbyId()).thenReturn("2131230973");

        cheatingController.manuallyEliminatePlayer(report);

        verify(mockPlayer).setActive(false);
        verify(messagingTemplate).convertAndSend(
                ("/topic/elimination/2131230973"),
                (Map.of("player", "Red", "reason", "CHEATING"))
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
    void testManuallyEliminatePlayer_playerNotFound_doesNothing() {
        CheatingReport report = new CheatingReport();
        report.setLobbyId("2131230973");
        report.setSuspect("Green");

        when(gameService.getGame("2131230973")).thenReturn(mockGameManager);
        when(mockGameManager.getPlayer("Green")).thenReturn(null);

        cheatingController.manuallyEliminatePlayer(report);

        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void testManuallyEliminatePlayer_playerAlreadyInactive_doesNothing() {
        CheatingReport report = new CheatingReport();
        report.setLobbyId("2131230973");
        report.setSuspect("Peacock");

        Player mockPlayer = mock(Player.class);
        when(mockPlayer.isActive()).thenReturn(false);

        when(gameService.getGame("2131230973")).thenReturn(mockGameManager);
        when(mockGameManager.getPlayer("Peacock")).thenReturn(mockPlayer);

        cheatingController.manuallyEliminatePlayer(report);

        verify(mockPlayer, never()).setActive(false);
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void testValidCheatingReport_ResetsSuspect() {
        when(gameService.getGame("2131230973")).thenReturn(mockGameManager);
        when(mockGameManager.getPlayer("Accuser")).thenReturn(accuserPlayer);
        when(mockGameManager.getPlayer("Suspect")).thenReturn(suspectPlayer);
        when(accuserPlayer.isCanReport()).thenReturn(true);

        when(mockGameManager.getLastSuggestion("Suspect"))
                .thenReturn(new GameManager.SuggestionRecord("Suspect", "Kitchen", "Knife"));
        when(mockGameManager.getCurrentRoom(suspectPlayer)).thenReturn("Kitchen");

        cheatingController.handleCheatingReport(report);

        verify(mockGameManager).resetPlayer(suspectPlayer);
        verify(messagingTemplate).convertAndSend(
                startsWith("/topic/playerReset/"),
                (Object) any()
        );
    }


    @Test
    void testFalseCheatingReport_ResetsAccuser() {
        when(gameService.getGame("2131230973")).thenReturn(mockGameManager);
        when(mockGameManager.getPlayer("Accuser")).thenReturn(accuserPlayer);
        when(mockGameManager.getPlayer("Suspect")).thenReturn(suspectPlayer);
        when(accuserPlayer.isCanReport()).thenReturn(true);

        when(mockGameManager.getLastSuggestion("Suspect"))
                .thenReturn(new GameManager.SuggestionRecord("Miss Scarlet", "Candlestick", "Ballroom"));
        when(mockGameManager.getCurrentRoom(suspectPlayer)).thenReturn("Kitchen");

        cheatingController.handleCheatingReport(report);

        verify(mockGameManager).resetPlayer(accuserPlayer);
        verify(accuserPlayer).setCanReport(false);
    }
    @Test
    void testPlayerCannotReportThemselves() {
        CheatingReport report = new CheatingReport();
        report.setLobbyId("2131230973");
        report.setAccuser("Scarlet");
        report.setSuspect("Scarlet");

        when(gameService.getGame("2131230973")).thenReturn(mockGameManager);
        when(mockGameManager.getPlayer("Scarlet")).thenReturn(accuserPlayer);
        when(accuserPlayer.isCanReport()).thenReturn(true);

        cheatingController.handleCheatingReport(report);

        verify(mockGameManager, never()).resetPlayer(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }
}


