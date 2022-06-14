package bg.softuni.tradezone.model.service;

import bg.softuni.tradezone.model.enums.Condition;
import bg.softuni.tradezone.model.enums.DeliveryType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AdvertisementServiceModel extends BaseServiceModel {

    private Long views;

    private String title;

    private BigDecimal price;

    private String description;

    private Condition condition;

    private DeliveryType delivery;

    private LocalDateTime createdOn;

    private ProfileServiceModel creator;

    private CategoryServiceModel category;

    private List<PhotoServiceModel> photos;

    private List<ProfileServiceModel> profilesWhichViewedIt;

    private List<ProfileServiceModel> profilesWhichLikedIt;
}
