package at.aau.se2.cluedo.services;

import at.aau.se2.cluedo.dto.AccusationRequest;
import at.aau.se2.cluedo.dto.SuggestionRequest;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.gameobjects.SecretFile;
import at.aau.se2.cluedo.models.lobby.Lobby;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 6;

    private final LobbyService lobbyService;
    protected final Map<String, GameManager> activeGames = new HashMap<>();

    @Autowired
    public GameService(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    public GameManager startGameFromLobby(String lobbyId) {
        Lobby lobby = lobbyService.getLobby(lobbyId);

        if (lobby == null) {
            logger.error("Failed to start game: Lobby {} not found", lobbyId);
            throw new IllegalArgumentException("Lobby not found");
        }

        List<Player> players = lobby.getPlayers();

        if (players.size() < MIN_PLAYERS) {
            logger.error("Failed to start game: Not enough players in lobby {}", lobbyId);
            throw new IllegalStateException("Not enough players to start a game.");
        }

        if (players.size() > MAX_PLAYERS) {
            logger.warn("Too many players in lobby {}. Truncating to max {}", lobbyId, MAX_PLAYERS);
            players = players.subList(0, MAX_PLAYERS);
        }

        GameManager gameManager = new GameManager(players);

        activeGames.put(lobbyId, gameManager);

        logger.info("Started new game from lobby {} with {} players", lobbyId, players.size());
        return gameManager;
    }

    public GameManager getGame(String lobbyId) {
        return activeGames.get(lobbyId);
    }

    public boolean canStartGame(String lobbyId) {
        Lobby lobby = lobbyService.getLobby(lobbyId);
        return lobby != null && lobby.getPlayers().size() >= MIN_PLAYERS;
    }

    public void processAccusation(AccusationRequest request) {
        GameManager game = activeGames.get(request.getLobbyId());
        if (game == null) {
            logger.warn("No active game found for lobby ID: {}", request.getLobbyId());
            return;
        }

        Player player = game.getPlayerList().stream()
                .filter(p -> p.getName().equalsIgnoreCase(request.getUsername()))
                .findFirst()
                .orElse(null);

        if (player == null) {
            logger.warn("Player {} not found in game.", request.getUsername());
            return;
        }

        if (!player.isActive()) {
            logger.info("Player {} is already eliminated.", player.getName());
            return;
        }

        SecretFile solution = game.getSecretFile();

        boolean isCorrect = solution.character().getCardName().equalsIgnoreCase(request.getSuspect()) &&
                solution.room().getCardName().equalsIgnoreCase(request.getRoom()) &&
                solution.weapon().getCardName().equalsIgnoreCase(request.getWeapon());

        if (isCorrect) {
            player.setHasWon(true);
            logger.info("Player {} made a correct accusation and won!", player.getName());
        } else {
            player.setActive(false);
            logger.info("Player {} guessed wrong and has been eliminated.", player.getName());
        }
    }
    public void performMovement(Player player, List<String> movement, String lobbId) {
        if(!movement.isEmpty()) {
            getGame(lobbId).performMovement(player, movement.get(0));
        }
    }
    public String makeAccusation(Player player, SecretFile accusation,String lobbyId) {

        getGame(lobbyId).makeAccusation(player,accusation);
        return "Wrong! " + player.getName() + " is out of the game!";
    }
    public String makeSuggestion(Player player, String suspect, String weapon,String lobbyId) {

        getGame(lobbyId).makeSuggestion(player,suspect, weapon);
        return "Nobody has any cards for " + player.getName() + " " + suspect + " " + weapon;
    }


    // For test access only
    protected Map<String, GameManager> getActiveGames() {
        return activeGames;
    }

    public void processSuggestion(SuggestionRequest request) {
        GameManager game = activeGames.get(request.getLobbyId());
        if (game == null) {
            logger.warn("No active game found for lobby ID: {}", request.getLobbyId());
            return;
        }

        Player player = game.getPlayer(request.getPlayerName());
        if (player == null) {
            logger.warn("Player {} not found in game.", request.getPlayerName());
            return;
        }

        boolean disproved = game.makeSuggestion(player, request.getSuspect(), request.getWeapon());

        logger.info("Suggestion by {}: {} in the {} with the {} — {}",
                request.getPlayerName(),
                request.getSuspect(),
                request.getRoom(),
                request.getWeapon(),
                disproved ? "Disproved" : "No one could disprove");
    }


}
