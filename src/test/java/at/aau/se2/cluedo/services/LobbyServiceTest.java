package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.dto.SolveCaseRequest;
import at.aau.se2.cluedo.models.cards.BasicCard;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import at.aau.se2.cluedo.models.lobby.Lobby;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LobbyServiceTest {

    @Mock
    private LobbyRegistry lobbyRegistry;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private LobbyService lobbyService;

    private final String HOST_USERNAME = "testHost";
    private final String PLAYER_USERNAME = "testPlayer";
    private final String TEST_LOBBY_ID = "test-lobby-id";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lobbyService = new LobbyService(lobbyRegistry, messagingTemplate);
    }

    @Test
    void createLobby_ShouldCreateNewLobbyWithHost() {
        Lobby mockLobby = new Lobby(TEST_LOBBY_ID, HOST_USERNAME);
        when(lobbyRegistry.createLobby(HOST_USERNAME)).thenReturn(mockLobby);
        String lobbyId = lobbyService.createLobby(HOST_USERNAME);

        assertEquals(TEST_LOBBY_ID, lobbyId);
        verify(lobbyRegistry).createLobby(HOST_USERNAME);
    }

    @Test
    void joinLobby_ShouldAddUserToLobby() {
        Lobby mockLobby = new Lobby(TEST_LOBBY_ID, HOST_USERNAME);
        when(lobbyRegistry.getLobby(TEST_LOBBY_ID)).thenReturn(mockLobby);
        lobbyService.joinLobby(TEST_LOBBY_ID, PLAYER_USERNAME);

        assertTrue(mockLobby.getParticipants().contains(PLAYER_USERNAME));
        verify(lobbyRegistry).getLobby(TEST_LOBBY_ID);
    }

    @Test
    void leaveLobby_ShouldRemoveUserFromLobby() {
        Lobby mockLobby = new Lobby(TEST_LOBBY_ID, HOST_USERNAME);
        mockLobby.addParticipant(PLAYER_USERNAME);
        when(lobbyRegistry.getLobby(TEST_LOBBY_ID)).thenReturn(mockLobby);
        lobbyService.leaveLobby(TEST_LOBBY_ID, PLAYER_USERNAME);

        assertFalse(mockLobby.getParticipants().contains(PLAYER_USERNAME));
        verify(lobbyRegistry).getLobby(TEST_LOBBY_ID);
    }

    @Test
    void getLobby_WithValidId_ShouldReturnLobby() {
        Lobby mockLobby = new Lobby(TEST_LOBBY_ID, HOST_USERNAME);
        when(lobbyRegistry.getLobby(TEST_LOBBY_ID)).thenReturn(mockLobby);
        Lobby lobby = lobbyService.getLobby(TEST_LOBBY_ID);

        assertNotNull(lobby);
        assertEquals(TEST_LOBBY_ID, lobby.getId());
        assertEquals(HOST_USERNAME, lobby.getHost());
        verify(lobbyRegistry).getLobby(TEST_LOBBY_ID);
    }

    @Test
    void testSolveCase_correctGuess_setsWinner() {
        Player player = new Player("Alice", "Blue", 0, 0);
        GameManager gameManager = new GameManager();
        gameManager.setPlayerList(new ArrayList<>(List.of(player)));
        gameManager.setCurrentPlayer(0);

        gameManager.setCurrentPlayer(0); // FIX

        BasicCard suspect = new BasicCard("Mrs. White", UUID.randomUUID(), "", "Character");
        BasicCard room = new BasicCard("Kitchen", UUID.randomUUID(), "", "Room");
        BasicCard weapon = new BasicCard("Knife", UUID.randomUUID(), "", "Weapon");
        SecretFile file = new SecretFile(room, weapon, suspect);
        gameManager.setSecretFile(file);

        Lobby lobby = new Lobby(TEST_LOBBY_ID, "Alice");
        lobby.setGameManager(gameManager);
        when(lobbyRegistry.getLobby(TEST_LOBBY_ID)).thenReturn(lobby);

        SolveCaseRequest request = new SolveCaseRequest();
        request.setLobbyId(TEST_LOBBY_ID);
        request.setUsername("Alice");
        request.setSuspect("Mrs. White");
        request.setRoom("Kitchen");
        request.setWeapon("Knife");

        lobbyService.solveCase(request);

        verify(messagingTemplate).convertAndSend("/topic/lobby/" + TEST_LOBBY_ID, "correct");
        assertEquals("Alice", lobby.getWinnerUsername());
    }

    @Test
    void testSolveCase_wrongGuess_eliminatesPlayer() {
        Player player = new Player("Bob", "Green", 0, 0);
        GameManager gameManager = new GameManager();
        gameManager.addPlayer(player);
        gameManager.setCurrentPlayer(0);

        BasicCard suspect = new BasicCard("Mrs. White", UUID.randomUUID(), "", "Character");
        BasicCard room = new BasicCard("Kitchen", UUID.randomUUID(), "", "Room");
        BasicCard weapon = new BasicCard("Knife", UUID.randomUUID(), "", "Weapon");
        SecretFile file = new SecretFile(room, weapon, suspect);
        gameManager.setSecretFile(file);

        Lobby lobby = new Lobby(TEST_LOBBY_ID, "Bob");
        lobby.setGameManager(gameManager);
        when(lobbyRegistry.getLobby(TEST_LOBBY_ID)).thenReturn(lobby);

        SolveCaseRequest request = new SolveCaseRequest();
        request.setLobbyId(TEST_LOBBY_ID);
        request.setUsername("Bob");
        request.setSuspect("Wrong");
        request.setRoom("Wrong");
        request.setWeapon("Wrong");

        lobbyService.solveCase(request);

        verify(messagingTemplate).convertAndSend("/topic/lobby/" + TEST_LOBBY_ID, "wrong");
        verify(messagingTemplate).convertAndSend(eq("/topic/lobby/" + TEST_LOBBY_ID + "/players"), any(Object.class));
        assertFalse(player.isActive());
    }

    @Test
    void testSolveCase_eliminatedPlayer_cannotSolveAgain() {
        Player eliminated = new Player("Elim", "Red", 0, 0);
        eliminated.setActive(false);

        GameManager gameManager = new GameManager();
        gameManager.setPlayerList(new ArrayList<>(List.of(eliminated)));
        gameManager.setCurrentPlayer(0);

        Lobby lobby = new Lobby(TEST_LOBBY_ID, "Elim");
        lobby.setGameManager(gameManager);
        when(lobbyRegistry.getLobby(TEST_LOBBY_ID)).thenReturn(lobby);

        SolveCaseRequest request = new SolveCaseRequest();
        request.setLobbyId(TEST_LOBBY_ID);
        request.setUsername("Elim");
        request.setSuspect("Mrs. White");
        request.setRoom("Kitchen");
        request.setWeapon("Knife");

        lobbyService.solveCase(request);

        verify(messagingTemplate).convertAndSend("/topic/lobby/" + TEST_LOBBY_ID, "already_eliminated");
    }

    @Test
    void testSolveCase_wrongPlayerBlocked() {
        Player current = new Player("Carol", "Red", 0, 0);
        GameManager gameManager = new GameManager();
        gameManager.setPlayerList(new ArrayList<>(List.of(current)));
        gameManager.setCurrentPlayer(0);
        gameManager.setCurrentPlayer(0);

        Lobby lobby = new Lobby(TEST_LOBBY_ID, "Carol");
        lobby.setGameManager(gameManager);
        when(lobbyRegistry.getLobby(TEST_LOBBY_ID)).thenReturn(lobby);

        SolveCaseRequest request = new SolveCaseRequest();
        request.setLobbyId(TEST_LOBBY_ID);
        request.setUsername("Eve");
        request.setSuspect("X");
        request.setRoom("Y");
        request.setWeapon("Z");

        lobbyService.solveCase(request);

        verify(messagingTemplate).convertAndSend("/topic/lobby/" + TEST_LOBBY_ID, "not_your_turn");
    }

    @Test
    void testSolveCase_lobbyNotFound() {
        when(lobbyRegistry.getLobby(TEST_LOBBY_ID)).thenReturn(null);

        SolveCaseRequest request = new SolveCaseRequest();
        request.setLobbyId(TEST_LOBBY_ID);
        request.setUsername("Nobody");
        request.setSuspect("X");
        request.setRoom("Y");
        request.setWeapon("Z");

        assertDoesNotThrow(() -> lobbyService.solveCase(request));
    }

    @Test
    void testSolveCase_gameManagerNotSet() {
        Lobby lobby = new Lobby(TEST_LOBBY_ID, "Dan");
        lobby.setGameManager(null);
        when(lobbyRegistry.getLobby(TEST_LOBBY_ID)).thenReturn(lobby);

        SolveCaseRequest request = new SolveCaseRequest();
        request.setLobbyId(TEST_LOBBY_ID);
        request.setUsername("Dan");
        request.setSuspect("X");
        request.setRoom("Y");
        request.setWeapon("Z");

        assertDoesNotThrow(() -> lobbyService.solveCase(request));
    }
}
