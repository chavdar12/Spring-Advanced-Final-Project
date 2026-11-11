package bg.softuni.tradezone.dto;

import bg.softuni.tradezone.entity.Condition;
import bg.softuni.tradezone.entity.DeliveryType;
import java.util.List;

public record AdvertisementOptions(List<Condition> conditions, List<DeliveryType> deliveries) {

  public static final AdvertisementOptions ALL =
      new AdvertisementOptions(List.of(Condition.values()), List.of(DeliveryType.values()));
}
