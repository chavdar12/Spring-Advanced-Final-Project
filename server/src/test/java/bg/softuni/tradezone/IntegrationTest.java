package bg.softuni.tradezone;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Base for integration tests: real PostgreSQL + Redis, shared by every test class through the
 * cached context.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class IntegrationTest {

  @ServiceConnection
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

  @ServiceConnection(name = "redis")
  static final GenericContainer<?> REDIS =
      new GenericContainer<>("redis:8-alpine").withExposedPorts(6379);

  static {
    POSTGRES.start();
    REDIS.start();
  }

  @Autowired protected MockMvc mockMvc;

  protected static RequestPostProcessor bearer(String token) {
    return request -> {
      request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
      return request;
    };
  }

  protected static JsonNode json(String body) {
    return JsonMapper.shared().readTree(body);
  }

  protected String signUpAndSignIn(String username) throws Exception {
    mockMvc
        .perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"username":"%s","email":"%s@mail.bg","password":"secret123"}
                    """
                        .formatted(username, username)))
        .andExpect(status().isCreated());

    String body =
        mockMvc
            .perform(
                post("/api/auth/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"username":"%s","password":"secret123"}
                        """
                            .formatted(username)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    return json(body).get("token").asString();
  }
}
