package bg.softuni.tradezone.dto;

import bg.softuni.tradezone.entity.Town;

public record TownResponse(Long id, String name, String regionName) {

  public static TownResponse from(Town town) {
    return town == null
        ? null
        : new TownResponse(town.getId(), town.getName(), town.getRegion().getName());
  }
}
