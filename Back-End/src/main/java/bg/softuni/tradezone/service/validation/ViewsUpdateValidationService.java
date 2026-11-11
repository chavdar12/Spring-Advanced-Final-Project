package bg.softuni.tradezone.service.validation;

import bg.softuni.tradezone.model.rest.ViewsUpdate;
import org.springframework.stereotype.Service;

@Service
public class ViewsUpdateValidationService implements ValidationService<ViewsUpdate> {

    @Override
    public boolean isValid(ViewsUpdate element) {
        return fieldsArePresent(element);
    }

    private boolean fieldsArePresent(ViewsUpdate element) {
        return element.getUsername() != null && element.getViews() != null;
    }
}
