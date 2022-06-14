package bg.softuni.tradezone.service.validation;

import bg.softuni.tradezone.model.rest.ProfileUpdate;
import org.springframework.stereotype.Service;

@Service
public class ProfileUpdateValidationService implements ValidationService<ProfileUpdate> {

    @Override
    public boolean isValid(ProfileUpdate element) {
        return true;
    }
}
