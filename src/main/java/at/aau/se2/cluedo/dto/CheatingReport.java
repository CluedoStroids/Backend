package at.aau.se2.cluedo.dto;

import lombok.Data;

@Data
public class CheatingReport {
    private String lobbyId;
    private String suspect;
    private String accuser;
}
