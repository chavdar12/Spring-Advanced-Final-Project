package bg.softuni.tradezone.dto;

import bg.softuni.tradezone.entity.Advertisement;
import bg.softuni.tradezone.entity.Condition;
import bg.softuni.tradezone.entity.DeliveryType;
import bg.softuni.tradezone.entity.UserProfile;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdvertisementDetails(
    Long id,
    String title,
    String description,
    BigDecimal price,
    Condition condition,
    DeliveryType delivery,
    long views,
    LocalDateTime createdOn,
    Long categoryId,
    String categoryName,
    List<Image> images,
    Seller seller) {

  public static AdvertisementDetails from(Advertisement ad) {
    return new AdvertisementDetails(
        ad.getId(),
        ad.getTitle(),
        ad.getDescription(),
        ad.getPrice(),
        ad.getCondition(),
        ad.getDelivery(),
        ad.getViews(),
        ad.getCreatedOn(),
        ad.getCategory().getId(),
        ad.getCategory().getName(),
        ad.getPhotos().stream().map(p -> new Image(p.getId(), p.getUrl())).toList(),
        Seller.from(ad.getCreator()));
  }

  public record Image(Long id, String url) {}

  public record Seller(
      String username, String firstName, String lastName, String aboutMe, String photoUrl) {

    static Seller from(UserProfile p) {
      return new Seller(
          p.getUsername(), p.getFirstName(), p.getLastName(), p.getAboutMe(), p.getPhotoUrl());
    }
  }
}
