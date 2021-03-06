package bg.softuni.tradezone.service.validation;

import bg.softuni.tradezone.model.rest.DeleteAdvImageRequest;
import org.springframework.stereotype.Service;

@Service
public class DeleteAdvImageRequestValidationService implements ValidationService<DeleteAdvImageRequest> {
    @Override
    public boolean isValid(DeleteAdvImageRequest element) {
        return fieldsArePresent(element);
    }

    private boolean fieldsArePresent(DeleteAdvImageRequest element) {
        return element.getPhotoId() != null && element.getAdvertisementId() != null
               && element.getUsername() != null;
    }
}
