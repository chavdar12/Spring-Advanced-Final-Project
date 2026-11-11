package bg.softuni.tradezone.repository;

import bg.softuni.tradezone.entity.ChatMessage;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  List<ChatMessage> findTop100ByChannelIdOrderByTimestampDesc(String channelId);

  @Modifying
  @Query(
      """
      update ChatMessage m set m.readDate = :now
      where m.channel.id = :channelId and m.sender.id <> :readerId and m.readDate is null
      """)
  void markRead(String channelId, Long readerId, LocalDateTime now);
}
