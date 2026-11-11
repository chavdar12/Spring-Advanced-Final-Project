package bg.softuni.tradezone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessage extends BaseEntity {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private UserProfile sender;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Channel channel;

  @NotBlank
  @Size(max = 1000)
  private String content;

  @NotNull private LocalDateTime timestamp = LocalDateTime.now();

  private LocalDateTime readDate;

  public ChatMessage(UserProfile sender, Channel channel, String content) {
    this.sender = sender;
    this.channel = channel;
    this.content = content;
  }
}
