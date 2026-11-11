package bg.softuni.tradezone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordRequest(
    @NotBlank String oldPassword,
    @NotBlank @Pattern(regexp = "\\S{3,16}", message = "must be 3-16 characters without spaces")
        String newPassword) {}
