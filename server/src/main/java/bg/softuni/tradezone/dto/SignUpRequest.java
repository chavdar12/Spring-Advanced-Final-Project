package bg.softuni.tradezone.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
    @NotBlank
        @Pattern(
            regexp = "[a-z][a-z0-9]{2,11}",
            message = "must be 3-12 lowercase letters or digits, starting with a letter")
        String username,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 3, max = 16) String password) {}
