package bg.softuni.tradezone.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bg.softuni.tradezone.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthFlowTests extends IntegrationTest {

  @Test
  void anonymousRequest_getsProblem401() throws Exception {
    mockMvc
        .perform(get("/api/regions"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.instance").value("/api/regions"));
  }

  @Test
  void forgedToken_isUnauthorized() throws Exception {
    mockMvc
        .perform(get("/api/regions").with(bearer("forged.token.value")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void signedInUser_seesSeededRegions() throws Exception {
    String token = signUpAndSignIn("ivan");

    mockMvc
        .perform(get("/api/regions").with(bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(28)));
  }

  @Test
  void verify_returnsUserForGateway_and401Otherwise() throws Exception {
    String token = signUpAndSignIn("verifier");

    mockMvc
        .perform(get("/api/auth/verify").with(bearer(token)))
        .andExpect(status().isNoContent())
        .andExpect(header().string("X-User", "verifier"));
    mockMvc.perform(get("/api/auth/verify")).andExpect(status().isUnauthorized());
  }

  @Test
  void longestAllowedUsername_canSignIn() throws Exception {
    signUpAndSignIn("abcdefghijkl");
  }

  @Test
  void invalidSignUp_listsEveryBadField() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"username":"X","email":"nope","password":""}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.errors.username").exists())
        .andExpect(jsonPath("$.errors.email").exists())
        .andExpect(jsonPath("$.errors.password").exists());
  }

  @Test
  void malformedJson_isProblem400() throws Exception {
    mockMvc
        .perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON).content("{"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void duplicateSignUp_isConflict() throws Exception {
    signUpAndSignIn("maria");

    mockMvc
        .perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"username":"maria","email":"other@mail.bg","password":"secret123"}
                    """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("Username is already taken"));
  }

  @Test
  void wrongPassword_isUnauthorizedProblem() throws Exception {
    signUpAndSignIn("georgi");

    mockMvc
        .perform(
            post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"username":"georgi","password":"wrong123"}
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Invalid username or password"));
  }

  @Test
  void plainUser_cannotReachAdminEndpoints() throws Exception {
    // the first three accounts get elevated roles, so make sure this one is a plain user
    for (String name : new String[] {"filler1", "filler2", "filler3"}) {
      signUpAndSignIn(name);
    }
    String token = signUpAndSignIn("plainuser");

    mockMvc
        .perform(get("/api/admin/users").with(bearer(token)))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void wrongCurrentPassword_isRejected() throws Exception {
    String token = signUpAndSignIn("pwchanger");

    mockMvc
        .perform(
            patch("/api/profile/password")
                .with(bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"oldPassword":"wrong","newPassword":"newpass1"}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("The current password is wrong"));
  }
}
