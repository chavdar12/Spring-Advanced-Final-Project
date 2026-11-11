package bg.softuni.tradezone.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationFlowTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private String signUpAndSignIn(String username) throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                         {"username":"%s","email":"%s@mail.bg","password":"secret123"}
                                         """.formatted(username, username)))
                .andExpect(status().isOk());

        String body = mockMvc.perform(post("/api/auth/signin")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content("""
                                                       {"username":"%s","password":"secret123"}
                                                       """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        return json.get("token").asText();
    }

    @Test
    void protectedEndpoint_rejectsAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/region/all")).andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_rejectsForgedToken() throws Exception {
        mockMvc.perform(get("/api/region/all").header("Authorization", "Bearer forged.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signedInUser_canReachProtectedEndpoints_andSeedDataLoadsFromClasspath() throws Exception {
        String token = signUpAndSignIn("ivan");

        mockMvc.perform(get("/api/region/all").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(28)));
    }

    @Test
    void duplicateSignUp_isRejected() throws Exception {
        signUpAndSignIn("maria");

        mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                         {"username":"maria","email":"other@mail.bg","password":"secret123"}
                                         """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void wrongPassword_isUnauthorized() throws Exception {
        signUpAndSignIn("georgi");

        mockMvc.perform(post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                         {"username":"georgi","password":"wrong123"}
                                         """))
                .andExpect(status().isUnauthorized());
    }
}
