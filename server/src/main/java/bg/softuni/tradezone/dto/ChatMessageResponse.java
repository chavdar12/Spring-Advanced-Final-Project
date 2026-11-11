package bg.softuni.tradezone.dto;

import bg.softuni.tradezone.entity.ChatMessage;
import java.time.LocalDateTime;

public record ChatMessageResponse(
    String channelId,
    String sender,
    String senderAvatarUrl,
    String content,
    LocalDateTime timestamp,
    LocalDateTime readDate) {

  public static ChatMessageResponse from(ChatMessage m) {
    return new ChatMessageResponse(
        m.getChannel().getId(),
        m.getSender().getUsername(),
        m.getSender().getPhotoUrl(),
        m.getContent(),
        m.getTimestamp(),
        m.getReadDate());
  }
}
