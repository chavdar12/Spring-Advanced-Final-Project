package bg.softuni.tradezone.web;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bg.softuni.tradezone.IntegrationTest;
import bg.softuni.tradezone.entity.Photo;
import bg.softuni.tradezone.service.PhotoService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class AdvertisementFlowTests extends IntegrationTest {

  private static final String IMAGE = "data:image/png;base64,iVBORw0KGgo=";
  private static final String AD =
      """
      {"title":"Red bicycle","description":"Barely used","price":120.50,"condition":"USED",
       "delivery":"COURIER","categoryId":1,"images":["%s"]}
      """
          .formatted(IMAGE);

  @MockitoBean PhotoService photoService;

  @BeforeEach
  void cloudinary() {
    when(photoService.upload(any()))
        .thenAnswer(i -> new Photo(UUID.randomUUID().toString(), "http://img/x.png"));
  }

  private String completedUser(String username) throws Exception {
    String token = signUpAndSignIn(username);
    mockMvc
        .perform(
            patch("/api/profile")
                .with(bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"firstName":"Ivan","lastName":"Ivanov","townId":1,"aboutMe":"Hi there"}
                    """))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            multipart(org.springframework.http.HttpMethod.PUT, "/api/profile/picture")
                .file(new MockMultipartFile("image", "me.png", "image/png", new byte[] {1}))
                .with(bearer(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.completed").value(true));
    return token;
  }

  private long createAd(String token) throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/advertisements")
                    .with(bearer(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(AD))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return json(body).get("id").asLong();
  }

  @Test
  void invalidAdvertisement_isRejectedBeforeAnyWork() throws Exception {
    String token = signUpAndSignIn("validator");

    mockMvc
        .perform(
            post("/api/advertisements")
                .with(bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"title":"x","description":"","price":-1,"condition":"NEW","categoryId":1,"images":["nope"]}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.title").exists())
        .andExpect(jsonPath("$.errors.description").exists())
        .andExpect(jsonPath("$.errors.price").exists())
        .andExpect(jsonPath("$.errors.delivery").value("must not be null"))
        .andExpect(jsonPath("$.errors['images[0]']").value("must be a PNG or JPEG data URL"));

    mockMvc
        .perform(
            post("/api/advertisements")
                .with(bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(AD.replace("USED", "SHINY")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.condition").value("has an invalid value"));
    verify(photoService, never()).upload(any());
  }

  @Test
  void incompleteProfile_cannotPublish() throws Exception {
    String token = signUpAndSignIn("incomplete");

    mockMvc
        .perform(
            post("/api/advertisements")
                .with(bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(AD))
        .andExpect(status().isForbidden())
        .andExpect(
            jsonPath("$.detail").value("Complete your profile before publishing advertisements"));
  }

  @Test
  void owner_publishesEditsAndDeletes_othersAreForbidden() throws Exception {
    String owner = completedUser("seller");
    String other = signUpAndSignIn("intruder");
    long id = createAd(owner);

    mockMvc
        .perform(get("/api/advertisements").with(bearer(other)).param("search", "red bicycle"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id", hasItem((int) id)));
    mockMvc
        .perform(
            get("/api/advertisements")
                .with(bearer(other))
                .param("category", "Vehicles")
                .param("condition", "USED")
                .param("sortBy", "price"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id", hasItem((int) id)));

    mockMvc
        .perform(
            put("/api/advertisements/{id}", id)
                .with(bearer(other))
                .contentType(MediaType.APPLICATION_JSON)
                .content(AD))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(delete("/api/advertisements/{id}", id).with(bearer(other)))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            put("/api/advertisements/{id}", id)
                .with(bearer(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(AD.replace("Red bicycle", "Blue bicycle")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Blue bicycle"));

    mockMvc
        .perform(delete("/api/advertisements/{id}", id).with(bearer(owner)))
        .andExpect(status().isNoContent());
    mockMvc
        .perform(get("/api/advertisements/{id}", id).with(bearer(owner)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Advertisement '%d' was not found".formatted(id)));
  }

  @Test
  void views_countOncePerUser_andFavoritesToggle() throws Exception {
    String owner = completedUser("viewed");
    String visitor = signUpAndSignIn("visitor");
    long id = createAd(owner);

    for (int i = 0; i < 2; i++) {
      mockMvc
          .perform(post("/api/advertisements/{id}/views", id).with(bearer(visitor)))
          .andExpect(status().isNoContent());
    }
    mockMvc
        .perform(get("/api/advertisements/{id}", id).with(bearer(visitor)))
        .andExpect(jsonPath("$.views").value(1));

    mockMvc
        .perform(post("/api/advertisements/{id}/favorite", id).with(bearer(visitor)))
        .andExpect(status().isNoContent());
    mockMvc
        .perform(get("/api/advertisements").with(bearer(visitor)).param("search", "red bicycle"))
        .andExpect(jsonPath("$.content[?(@.id == %d)].likedBy[0]".formatted(id)).value("visitor"));
  }

  @Test
  void searchRejectsUnknownSortField() throws Exception {
    String token = signUpAndSignIn("sorter");

    mockMvc
        .perform(get("/api/advertisements").with(bearer(token)).param("sortBy", "password"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.sortBy").exists());
  }
}
