package bg.softuni.tradezone.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bg.softuni.tradezone.IntegrationTest;
import org.junit.jupiter.api.Test;

class ChatTests extends IntegrationTest {

  @Test
  void participant_opensChannelRepeatedly_outsiderIsForbidden() throws Exception {
    String alice = signUpAndSignIn("alice");
    signUpAndSignIn("bob");
    String eve = signUpAndSignIn("eve");

    for (int i = 0; i < 2; i++) { // opening is idempotent
      mockMvc
          .perform(post("/api/chat/channels/alice.bob").with(bearer(alice)))
          .andExpect(status().isNoContent());
    }
    mockMvc
        .perform(get("/api/chat/channels/alice.bob/messages").with(bearer(alice)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());

    mockMvc
        .perform(get("/api/chat/channels/alice.bob/messages").with(bearer(eve)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.detail").value("You are not part of this conversation"));
  }
}
