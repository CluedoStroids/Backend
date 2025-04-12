package at.aau.se2.cluedo.models.gameobjects;

import at.aau.se2.cluedo.models.cards.BasicCard;

// The Secret File where the solution is written.
public record SecretFile(BasicCard room, BasicCard weapon, BasicCard character) {
}
