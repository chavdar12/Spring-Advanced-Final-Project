package bg.softuni.tradezone.service;

import bg.softuni.tradezone.entity.Photo;
import bg.softuni.tradezone.error.ApiException;
import com.cloudinary.Uploader;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Stores images in Cloudinary. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoService {

  private final Uploader uploader;

  /**
   * @param image a data URL string or raw bytes
   */
  public Photo upload(Object image) {
    try {
      Map<?, ?> result = uploader.upload(image, Map.of());
      return new Photo(String.valueOf(result.get("public_id")), String.valueOf(result.get("url")));
    } catch (Exception e) {
      log.warn("Image upload failed: {}", e.getMessage());
      throw ApiException.badGateway("The image could not be uploaded, try again later");
    }
  }

  // shortcut: runs before the transaction commits, so a rollback leaves a dangling DB row; move to
  // an after-commit hook if that matters
  public void delete(Photo photo) {
    try {
      uploader.destroy(photo.getIdInCloud(), Map.of());
    } catch (Exception e) {
      log.warn(
          "Could not delete image {} from the cloud: {}", photo.getIdInCloud(), e.getMessage());
    }
  }
}
