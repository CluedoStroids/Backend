package at.aau.serg.websocketdemoserver.GameObjects;

import at.aau.serg.websocketdemoserver.GameObjects.Cards.BasicCard;
import lombok.Getter;
import lombok.Setter;

// The Secret File where the solution is written.
public record SecretFile(BasicCard room, BasicCard weapon, BasicCard character) {
}
