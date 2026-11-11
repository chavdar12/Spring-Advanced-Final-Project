package bg.softuni.tradezone.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ImagesRequest(@NotEmpty @Size(max = 10) List<@ImageData String> images) {}
