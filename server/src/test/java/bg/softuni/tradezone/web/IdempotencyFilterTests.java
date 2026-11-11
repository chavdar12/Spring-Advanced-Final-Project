package bg.softuni.tradezone.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bg.softuni.tradezone.IntegrationTest;
import bg.softuni.tradezone.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class IdempotencyFilterTests extends IntegrationTest {

  private static final String SIGNUP =
      """
      {"username":"idem","email":"idem@mail.bg","password":"secret123"}
      """;

  @Autowired UserRepository userRepository;

  @Test
  void retriedRequest_isReplayed_notExecutedTwice() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/signup")
                .header(IdempotencyFilter.HEADER, "k-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(SIGNUP))
        .andExpect(status().isCreated())
        .andExpect(header().doesNotExist("Idempotent-Replayed"));

    // without the filter this second call would fail with 409 "username already taken"
    mockMvc
        .perform(
            post("/api/auth/signup")
                .header(IdempotencyFilter.HEADER, "k-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(SIGNUP))
        .andExpect(status().isCreated())
        .andExpect(header().string("Idempotent-Replayed", "true"));

    assertEquals(
        1, userRepository.findAll().stream().filter(u -> u.getUsername().equals("idem")).count());
  }

  @Test
  void sameKey_differentPayload_isRejected() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/signup")
                .header(IdempotencyFilter.HEADER, "k-2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"username":"idem2","email":"idem2@mail.bg","password":"secret123"}
                    """))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/auth/signup")
                .header(IdempotencyFilter.HEADER, "k-2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"username":"idem3","email":"idem3@mail.bg","password":"secret123"}
                    """))
        .andExpect(status().isUnprocessableContent())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(
            jsonPath("$.detail")
                .value("This Idempotency-Key was already used with a different payload"));
  }

  @Test
  void oversizedKey_isBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/signup")
                .header(IdempotencyFilter.HEADER, "x".repeat(256))
                .contentType(MediaType.APPLICATION_JSON)
                .content(SIGNUP))
        .andExpect(status().isBadRequest());
  }
}
