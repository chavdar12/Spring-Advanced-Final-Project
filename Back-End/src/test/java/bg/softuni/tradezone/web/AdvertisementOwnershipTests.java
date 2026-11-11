package bg.softuni.tradezone.web;

import bg.softuni.tradezone.model.rest.AdvertisementEditedModel;
import bg.softuni.tradezone.model.rest.ImagesToUploadModel;
import bg.softuni.tradezone.service.base.AdvertisementService;
import bg.softuni.tradezone.service.base.PhotoService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "attacker", roles = "USER")
class AdvertisementOwnershipTests {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AdvertisementService advertisementService;

    @MockitoBean
    PhotoService photoService;

    @Test
    void edit_usesAuthenticatedUser_notCreatorFromBody() throws Exception {
        mockMvc.perform(put("/api/edit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"id\":1,\"creator\":\"victim\",\"title\":\"Hacked title\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<AdvertisementEditedModel> captor = ArgumentCaptor.forClass(AdvertisementEditedModel.class);
        verify(advertisementService).edit(captor.capture());
        assertEquals("attacker", captor.getValue().getCreator());
    }

    @Test
    void uploadImages_usesAuthenticatedUser_notUsernameFromBody() throws Exception {
        mockMvc.perform(patch("/api/upload-images")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"advertisementId\":1,\"username\":\"victim\",\"images\":[\"x\"]}"))
                .andExpect(status().isOk());

        ArgumentCaptor<ImagesToUploadModel> captor = ArgumentCaptor.forClass(ImagesToUploadModel.class);
        verify(photoService).uploadAdvertisementPhotos(captor.capture());
        assertEquals("attacker", captor.getValue().getUsername());
    }
}
