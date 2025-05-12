package at.aau.se2.cluedo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IsWallRequest {
    @NotNull(message = "X information cannot be null")
    public int x;
    @NotNull(message = "Y information cannot be null")
    public int y;
}
