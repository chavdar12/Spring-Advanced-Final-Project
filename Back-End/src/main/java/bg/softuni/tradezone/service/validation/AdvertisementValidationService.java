package bg.softuni.tradezone.service.validation;

import bg.softuni.tradezone.model.enums.Condition;
import bg.softuni.tradezone.model.enums.DeliveryType;
import bg.softuni.tradezone.model.rest.AdvertisementBaseModel;
import bg.softuni.tradezone.model.rest.AdvertisementCreateModel;
import org.springframework.stereotype.Service;

@Service
public class AdvertisementValidationService implements ValidationService<AdvertisementBaseModel> {

    private static final String TITLE_MATCHER = "^[A-ZА-Я][A-ZА-Яа-яa-z\\s\\d]{4,19}$";

    @Override
    public boolean isValid(AdvertisementBaseModel advertisement) {
        return fieldsArePresent(advertisement) && fieldsAreValid(advertisement);
    }

    private boolean fieldsArePresent(AdvertisementBaseModel model) {
        return model.getCreator() != null && model.getCondition() != null && model.getCategory() != null
               && model.getDelivery() != null && model.getDescription() != null && model.getPrice() != null && model.getTitle() != null;
    }

    private boolean fieldsAreValid(AdvertisementBaseModel advertisement) {

        if (!advertisement.getTitle().matches(TITLE_MATCHER)) {
            return false;
        }

        if (advertisement instanceof AdvertisementCreateModel createModel
            && (createModel.getImages() == null || createModel.getImages().length < 1)) {
            return false;
        }

        try {
            Condition.valueOf(advertisement.getCondition());
            DeliveryType.valueOf(advertisement.getDelivery());
        } catch (IllegalArgumentException ex) {
            return false;
        }

        return true;
    }

}
