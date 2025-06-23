package at.aau.se2.cluedo.dto;

public class SolveCaseRequest {
    private String lobbyId;
    private String username;
    private String suspect;
    private String room;
    private String weapon;

    public SolveCaseRequest() {}

    public SolveCaseRequest(String lobbyId, String suspect, String room, String weapon, String username) {
        this.lobbyId = lobbyId;
        this.suspect = suspect;
        this.room = room;
        this.weapon = weapon;
        this.username = username;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    public String getSuspect() {
        return suspect;
    }

    public void setSuspect(String suspect) {
        this.suspect = suspect;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getWeapon() {
        return weapon;
    }

    public void setWeapon(String weapon) {
        this.weapon = weapon;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
