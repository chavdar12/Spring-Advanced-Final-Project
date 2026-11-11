package bg.softuni.tradezone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(@NotBlank @Size(max = 12) String username, @NotBlank String password) {}
