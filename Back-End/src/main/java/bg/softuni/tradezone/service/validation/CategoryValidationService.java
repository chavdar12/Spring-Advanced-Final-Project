package bg.softuni.tradezone.service.validation;

import bg.softuni.tradezone.model.service.CategoryServiceModel;
import org.springframework.stereotype.Service;

@Service
public class CategoryValidationService implements ValidationService<CategoryServiceModel> {

    @Override
    public boolean isValid(CategoryServiceModel element) {
        return true;
    }
}
