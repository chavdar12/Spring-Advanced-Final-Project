package bg.softuni.tradezone.service;

import bg.softuni.tradezone.dto.ChatMessageRequest;
import bg.softuni.tradezone.dto.ChatMessageResponse;
import bg.softuni.tradezone.dto.ChatUser;
import bg.softuni.tradezone.entity.Channel;
import bg.softuni.tradezone.entity.ChatMessage;
import bg.softuni.tradezone.entity.UserProfile;
import bg.softuni.tradezone.error.ApiException;
import bg.softuni.tradezone.repository.ChannelRepository;
import bg.softuni.tradezone.repository.ChatMessageRepository;
import bg.softuni.tradezone.repository.UserProfileRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** One-to-one chat; a channel id is both usernames, sorted and joined by a dot. */
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

  private final ChannelRepository channels;
  private final ChatMessageRepository messages;
  private final UserProfileRepository profiles;
  private final SimpMessagingTemplate messaging;

  public static boolean isParticipant(String channelId, String username) {
    String[] users = channelId.split("\\.");
    return users.length == 2
        && users[0].compareTo(users[1]) < 0
        && (users[0].equals(username) || users[1].equals(username));
  }

  private static void requireParticipant(String channelId, String username) {
    if (!isParticipant(channelId, username)) {
      throw ApiException.forbidden("You are not part of this conversation");
    }
  }

  @Transactional(readOnly = true)
  public List<ChatUser> users(String username) {
    return profiles.findAllByIsCompletedTrueAndUserUsernameNot(username).stream()
        .map(ChatUser::from)
        .toList();
  }

  /** Creates the channel if needed and subscribes the user to it; safe to repeat. */
  public void open(String username, String channelId) {
    requireParticipant(channelId, username);
    profiles.getByUsername(username).getSubscribedTo().add(channel(channelId));
  }

  @Transactional(readOnly = true)
  public List<ChatMessageResponse> history(String username, String channelId) {
    requireParticipant(channelId, username);
    return messages.findTop100ByChannelIdOrderByTimestampDesc(channelId).reversed().stream()
        .map(ChatMessageResponse::from)
        .toList();
  }

  public void markRead(String username, String channelId) {
    requireParticipant(channelId, username);
    messages.markRead(channelId, profiles.getByUsername(username).getId(), LocalDateTime.now());
  }

  public void send(String username, ChatMessageRequest request) {
    requireParticipant(request.channelId(), username);
    UserProfile sender = profiles.getByUsername(username);
    ChatMessage message =
        messages.save(new ChatMessage(sender, channel(request.channelId()), request.content()));
    messaging.convertAndSend(
        "/channel/chat/" + request.channelId(), ChatMessageResponse.from(message));
  }

  private Channel channel(String id) {
    return channels.findById(id).orElseGet(() -> channels.save(new Channel(id)));
  }
}
