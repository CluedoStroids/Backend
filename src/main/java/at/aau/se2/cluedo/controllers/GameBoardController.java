package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.GameDataResponse;
import at.aau.se2.cluedo.dto.IsWallRequest;
import at.aau.se2.cluedo.dto.PerformMoveRequest;
import at.aau.se2.cluedo.dto.StartGameRequest;
import at.aau.se2.cluedo.models.gameboard.CellType;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.models.lobby.Lobby;
import at.aau.se2.cluedo.services.GameService;
import at.aau.se2.cluedo.services.LobbyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class GameBoardController {

    private static final Logger logger = LoggerFactory.getLogger(GameBoardController.class);
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameService gameService;

    @Autowired
    private LobbyService lobbyService;

    public GameBoardController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/getPlayers")
    public void getPlayers() {
        List<Player> players = LobbyController.getInstance().getLobbyService().getAllActiveLobbies().get(0).getPlayers();
        messagingTemplate.convertAndSend("/topic/players",players);
        logger.info("Players");
    }
    @MessageMapping("/performMovement/{lobbyId}")
    @SendTo("/topic/performMovement/{lobbyId}")
    public GameDataResponse performMovement( @DestinationVariable String lobbyId, PerformMoveRequest request){
        logger.info("Player {} is attempting to move {} {}", request.getPlayer().getName(), lobbyId,lobbyService.gameService.getGame(lobbyId).getPlayer(request.getPlayer().getName()));
        lobbyService.performMovement(request.getPlayer(),request.getMoves(),lobbyId);
        logger.info("Player X{} Y{} second X{} Y{}",request.getPlayer().getX(),request.getPlayer().getY(),gameService.getGame(lobbyId).getPlayer(request.getPlayer().getName()).getX(),gameService.getGame(lobbyId).getPlayer(request.getPlayer().getName()).getY());
        gameService.getGame(lobbyId).nextTurn();
        StartGameRequest response = new StartGameRequest();

        response.setPlayer(gameService.getGame(lobbyId).getPlayer(request.getPlayer().getName()));

        return gameData(lobbyId,response);
    }
    @MessageMapping("/isWall/{lobbyId}")
    @SendTo("/topic/isWall/{lobbyId}")
    public boolean isWall(@DestinationVariable String lobbyId, IsWallRequest request){

        CellType temp = gameService.getGame(lobbyId).getGameBoard().getCell(request.x,request.y).getCellType();
        if(temp.equals(CellType.ROOM)){
            logger.info("Player ran against wall X{} Y{}",request.x,request.y);
            return true;

        }
        logger.info("Player did not ran against wall X{} Y{}",request.x,request.y);
        return false;


    }
    @MessageMapping("/getGameData/{lobbyId}")
    @SendTo("/topic/gameData/{lobbyId}")
    public GameDataResponse gameData(@DestinationVariable String lobbyId, StartGameRequest request) {
        Player initiator = request.getPlayer();
        logger.info("Player {} is attempting to get Data from lobby {}", initiator.getName(), lobbyId);

        Lobby lobby = lobbyService.getLobby(lobbyId);

        Player lobbyPlayer = lobby.getPlayers().stream()
                .filter(p -> p.getName().equals(initiator.getName()))
                .findFirst()
                .orElse(null);
        try {
            GameManager gameManager = gameService.getGame(lobbyId);
            logger.info("Game Data successfully from lobby {}", lobbyId);
            List<Player> gamePlayers = gameManager.getPlayers();
            for (Player p: gamePlayers) {
                logger.info("Positions {} {} {}",p.getName(),p.getX(),p.getY());
            }

            return new GameDataResponse(lobbyId, gamePlayers,gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()));
        } catch (Exception e) {
            logger.error("Failed to get GameData from lobby {}: {}", lobbyId, e.getMessage());
            throw new IllegalStateException("Failed to get game Data: " + e.getMessage());
        }
    }

}
