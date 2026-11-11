package bg.softuni.tradezone.web;

import bg.softuni.tradezone.dto.ChatMessageRequest;
import bg.softuni.tradezone.dto.ChatMessageResponse;
import bg.softuni.tradezone.dto.ChatUser;
import bg.softuni.tradezone.service.ChatService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chat;

  @GetMapping("/users")
  public List<ChatUser> users(Principal principal) {
    return chat.users(principal.getName());
  }

  @PostMapping("/channels/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void open(@PathVariable String id, Principal principal) {
    chat.open(principal.getName(), id);
  }

  @GetMapping("/channels/{id}/messages")
  public List<ChatMessageResponse> history(@PathVariable String id, Principal principal) {
    return chat.history(principal.getName(), id);
  }

  @PostMapping("/channels/{id}/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markRead(@PathVariable String id, Principal principal) {
    chat.markRead(principal.getName(), id);
  }

  /** STOMP: /app/messages */
  @MessageMapping("/messages")
  public void send(@Valid @Payload ChatMessageRequest message, Principal principal) {
    chat.send(principal.getName(), message);
  }
}
