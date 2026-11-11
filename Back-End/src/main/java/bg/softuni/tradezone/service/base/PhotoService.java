package bg.softuni.tradezone.service.base;

import bg.softuni.tradezone.model.rest.ImagesToUploadModel;
import bg.softuni.tradezone.model.rest.message.response.ResponseMessage;
import bg.softuni.tradezone.model.service.PhotoServiceModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PhotoService {

    PhotoServiceModel upload(String imageContent);

    PhotoServiceModel upload(MultipartFile file);

    List<PhotoServiceModel> uploadAdvertisementPhotos(ImagesToUploadModel images);

    ResponseMessage delete(Long id);

    void deleteAll(List<Long> ids);
}
