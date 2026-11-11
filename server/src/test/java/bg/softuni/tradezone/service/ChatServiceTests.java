package bg.softuni.tradezone.service;

import static bg.softuni.tradezone.service.ChatService.isParticipant;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChatServiceTests {

  @Test
  void onlyTheTwoUsersOfASortedChannelIdTakePart() {
    assertTrue(isParticipant("alice.bob", "alice"));
    assertTrue(isParticipant("alice.bob", "bob"));
    assertFalse(isParticipant("alice.bob", "eve"));
    assertFalse(isParticipant("bob.alice", "bob")); // unsorted ids would create duplicate channels
    assertFalse(isParticipant("alice.alice", "alice"));
    assertFalse(isParticipant("alice.bob.eve", "eve"));
  }
}
