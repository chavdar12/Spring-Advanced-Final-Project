package bg.softuni.tradezone.dto;

import bg.softuni.tradezone.entity.Advertisement;
import bg.softuni.tradezone.entity.Condition;
import bg.softuni.tradezone.entity.UserProfile;
import java.math.BigDecimal;
import java.util.List;

public record AdvertisementSummary(
    Long id,
    String title,
    String description,
    BigDecimal price,
    Condition condition,
    long views,
    String imageUrl,
    String creator,
    List<String> likedBy) {

  public static AdvertisementSummary from(Advertisement ad) {
    return new AdvertisementSummary(
        ad.getId(),
        ad.getTitle(),
        ad.getDescription(),
        ad.getPrice(),
        ad.getCondition(),
        ad.getViews(),
        ad.getImageUrl(),
        ad.getCreator().getUsername(),
        ad.getLikedBy().stream().map(UserProfile::getUsername).toList());
  }
}
