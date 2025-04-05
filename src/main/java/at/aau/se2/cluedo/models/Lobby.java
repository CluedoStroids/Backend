package at.aau.se2.cluedo.models;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    private String id;
    private String host;
    private List<String> participants = new ArrayList<>();

    public Lobby() {
    }

    public Lobby(String id, String host) {
        this.id = id;
        this.host = host;
        this.participants.add(host);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }
}