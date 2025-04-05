package GameBoard;

import java.util.ArrayList;
import java.util.List;

public class Room {
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

    public String getName() {
        return name;
    }

    public List<Player> getPlayersInRoom() {
        return new ArrayList<>(playersInRoom);
    }
}