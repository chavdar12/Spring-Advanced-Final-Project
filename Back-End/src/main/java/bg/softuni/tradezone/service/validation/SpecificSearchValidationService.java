package bg.softuni.tradezone.service.validation;

import bg.softuni.tradezone.model.rest.search.SpecificSearch;
import org.springframework.stereotype.Service;

@Service
public class SpecificSearchValidationService implements ValidationService<SpecificSearch> {
    @Override
    public boolean isValid(SpecificSearch element) {
        return true;
    }
}
