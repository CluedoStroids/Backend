package at.aau.se2.cluedo.models.gameboard;

import at.aau.se2.cluedo.models.gameobjects.Player;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Room {
    @Getter
    private final String name;
    private final List<Player> playersInRoom;

    public Room(String name) {
        this.name = name;
        this.playersInRoom = new ArrayList<>();
    }

    public void playerEntersRoom(Player player) {
        playersInRoom.add(player);
    }

    public void playerLeavesRoom(Player player) {
        playersInRoom.remove(player);
    }

    public List<Player> getPlayersInRoom() {
        return new ArrayList<>(playersInRoom);
    }

}
