package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.CheatingReport;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.services.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheatingControllerTest {

    private GameService gameService;
    private SimpMessagingTemplate messagingTemplate;
    private CheatingController cheatingController;
    private GameManager mockGameManager;

    @BeforeEach
    void setUp() {
        gameService = mock(GameService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        cheatingController = new CheatingController(gameService, messagingTemplate);
        mockGameManager = mock(GameManager.class);
    }

    @Test
    void testHandleCheatingReport_sendsMessageAndRecordsReport() {
        // Arrange
        CheatingReport report = new CheatingReport();
        report.setLobbyId("lobby123");
        report.setAccuser("Alice");
        report.setSuspect("Bob");

        when(gameService.getGame("lobby123")).thenReturn(mockGameManager);
        when(mockGameManager.getCheatingReportsCount("Bob")).thenReturn(1);

        // Act
        cheatingController.handleCheatingReport(report);

        // Assert
        verify(mockGameManager).reportCheating("Alice", "Bob");
        verify(messagingTemplate).convertAndSend(
                eq("/topic/cheating/lobby123"),
                eq(Map.of("type", "CHEATING_REPORT", "suspect", "Bob", "accuser", "Alice"))
        );
    }

    @Test
    void testManuallyEliminatePlayer_sendsEliminationMessage() {
        // Arrange
        CheatingReport report = new CheatingReport();
        report.setLobbyId("lobby123");
        report.setSuspect("Bob");

        Player mockPlayer = mock(Player.class);
        when(mockPlayer.isActive()).thenReturn(true);

        when(gameService.getGame("lobby123")).thenReturn(mockGameManager);
        when(mockGameManager.getPlayer("Bob")).thenReturn(mockPlayer);

        // Act
        cheatingController.manuallyEliminatePlayer(report);

        // Assert
        verify(mockPlayer).setActive(false);
        verify(messagingTemplate).convertAndSend(
                eq("/topic/elimination/lobby123"),
                eq(Map.of("player", "Bob", "reason", "CHEATING"))
        );
    }
}

