package bg.softuni.tradezone.dto;

import bg.softuni.tradezone.entity.Category;

public record CategoryResponse(Long id, String name, String photoUrl) {

  public static CategoryResponse from(Category category) {
    return new CategoryResponse(category.getId(), category.getName(), category.getPhoto().getUrl());
  }
}
