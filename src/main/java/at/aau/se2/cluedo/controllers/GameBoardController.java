package at.aau.se2.cluedo.controllers;

import at.aau.se2.cluedo.dto.GameDataResponse;
import at.aau.se2.cluedo.dto.IsWallRequest;
import at.aau.se2.cluedo.dto.PerformMoveRequest;
import at.aau.se2.cluedo.dto.StartGameRequest;
import at.aau.se2.cluedo.models.gameboard.CellType;
import at.aau.se2.cluedo.models.gamemanager.GameManager;
import at.aau.se2.cluedo.models.gameobjects.Player;
import at.aau.se2.cluedo.services.GameService;
import at.aau.se2.cluedo.services.LobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class GameBoardController {

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

    }
    @MessageMapping("/performMovement/{lobbyId}")
    @SendTo("/topic/performMovement/{lobbyId}")
    public GameDataResponse performMovement( @DestinationVariable String lobbyId, PerformMoveRequest request){

        gameService.performMovement(request.getPlayer(),request.getMoves(),lobbyId);

        gameService.getGame(lobbyId).nextTurn();
        StartGameRequest response = new StartGameRequest();

        response.setPlayer(gameService.getGame(lobbyId).getPlayer(request.getPlayer().getName()));

        return gameData(lobbyId,response);
    }
    @MessageMapping("/isWall/{lobbyId}")
    @SendTo("/topic/isWall/{lobbyId}")
    public boolean isWall(@DestinationVariable String lobbyId, IsWallRequest request){

        CellType temp = gameService.getGame(lobbyId).getGameBoard().getCell(request.x,request.y).getCellType();
        return temp.equals(CellType.ROOM);


    }
    @MessageMapping("/getGameData/{lobbyId}")
    @SendTo("/topic/gameData/{lobbyId}")
    public GameDataResponse gameData(@DestinationVariable String lobbyId, StartGameRequest request) {


        try {
            GameManager gameManager = gameService.getGame(lobbyId);

            List<Player> gamePlayers = gameManager.getPlayers();
            lobbyService.getLobby(lobbyId).setPlayers(gamePlayers);


            return new GameDataResponse(lobbyId, gamePlayers,gameManager.getPlayers().get(gameManager.getCurrentPlayerIndex()));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get game Data: " + e.getMessage());
        }
    }

}
