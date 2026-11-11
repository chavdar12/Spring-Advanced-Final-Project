package bg.softuni.tradezone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
    @NotBlank String channelId, @NotBlank @Size(max = 1000) String content) {}
