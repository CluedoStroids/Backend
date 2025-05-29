package at.aau.se2.cluedo.dto;

public class SuggestionRequest {
    private String lobbyId;
    private String suspect;
    private String weapon;
    private String room;
    private String playerName;

    public SuggestionRequest() {}

    public SuggestionRequest(String lobbyId, String suspect, String weapon, String room, String playerName) {
        this.lobbyId = lobbyId;
        this.suspect = suspect;
        this.weapon = weapon;
        this.room = room;
        this.playerName = playerName;
    }

    public String getLobbyId() { return lobbyId; }
    public void setLobbyId(String lobbyId) { this.lobbyId = lobbyId; }

    public String getSuspect() { return suspect; }
    public void setSuspect(String suspect) { this.suspect = suspect; }

    public String getWeapon() { return weapon; }
    public void setWeapon(String weapon) { this.weapon = weapon; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
}
