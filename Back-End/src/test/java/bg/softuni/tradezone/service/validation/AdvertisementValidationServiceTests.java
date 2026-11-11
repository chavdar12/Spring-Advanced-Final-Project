package bg.softuni.tradezone.service.validation;

import bg.softuni.tradezone.model.rest.AdvertisementBaseModel;
import bg.softuni.tradezone.model.rest.AdvertisementCreateModel;
import bg.softuni.tradezone.model.rest.AdvertisementEditedModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdvertisementValidationServiceTests {

    private static final int MIN_TITLE_LENGTH = 5;
    private static final int MAX_TITLE_LENGTH = 20;

    AdvertisementValidationService advertisementValidationService;

    AdvertisementBaseModel advertisement;

    @BeforeEach
    void init() {
        advertisementValidationService = new AdvertisementValidationService();
        advertisement = new AdvertisementCreateModel();
        advertisement.setCategory(1L);
        advertisement.setCondition("NEW");
        advertisement.setCreator("creator");
        advertisement.setDelivery("COURIER");
        advertisement.setDescription("lorem ipsum");
        advertisement.setPrice(BigDecimal.valueOf(10));
        advertisement.setTitle("Audi A6");
        ((AdvertisementCreateModel) advertisement).setImages(new String[]{"image"});
    }

    @Test
    void isValid_shouldReturnTrue_whenValidModel() {
        assertTrue(advertisementValidationService.isValid(advertisement));
    }

    @Test
    void isValid_shouldReturnFalse_whenTitleStartsWithLowercaseLetter() {
        advertisement.setTitle("invalid");
        assertFalse(advertisementValidationService.isValid(advertisement));
    }

    @Test
    void isValid_shouldReturnFalse_whenTitleLengthMoreThanMAX() {
        advertisement.setTitle("invaliddddddddddddddddd");
        assertFalse(advertisementValidationService.isValid(advertisement));
    }

    @Test
    void isValid_shouldReturnFalse_whenTitleLengthLessThanMIN() {
        advertisement.setTitle("inva");
        assertFalse(advertisementValidationService.isValid(advertisement));
    }

    @Test
    void isValid_shouldReturnFalse_whenHasNoImages() {
        ((AdvertisementCreateModel)advertisement).setImages(null);
        assertFalse(advertisementValidationService.isValid(advertisement));
    }

    @Test
    void isValid_shouldReturnFalse_whenInvalidCondition() {
        advertisement.setCondition("invalid");
        assertFalse(advertisementValidationService.isValid(advertisement));
    }

    @Test
    void isValid_shouldReturnFalse_whenInvalidDeliveryType() {
        advertisement.setDelivery("invalid");
        assertFalse(advertisementValidationService.isValid(advertisement));
    }

    @Test
    void isValid_shouldReturnFalse_whenDeliveryMissing() {
        advertisement.setDelivery(null);
        assertFalse(advertisementValidationService.isValid(advertisement));
    }

    @Test
    void isValid_shouldNotRequireImages_forEditModel() {
        AdvertisementEditedModel edited = new AdvertisementEditedModel();
        edited.setCategory(1L);
        edited.setCondition("NEW");
        edited.setCreator("creator");
        edited.setDelivery("COURIER");
        edited.setDescription("lorem ipsum");
        edited.setPrice(BigDecimal.TEN);
        edited.setTitle("Audi A6");
        assertTrue(advertisementValidationService.isValid(edited));
    }
}
