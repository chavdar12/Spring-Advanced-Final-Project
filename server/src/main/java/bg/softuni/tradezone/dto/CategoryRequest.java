package bg.softuni.tradezone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
    @NotBlank @Size(max = 50) String name, @NotBlank @ImageData String image) {}
