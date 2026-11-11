package bg.softuni.tradezone.dto;

import bg.softuni.tradezone.entity.RoleName;
import bg.softuni.tradezone.entity.User;

public record AdminUserResponse(Long id, String username, String photoUrl, RoleName role) {

  public static AdminUserResponse from(User user) {
    return new AdminUserResponse(
        user.getId(), user.getUsername(), user.getProfile().getPhotoUrl(), user.topRole());
  }
}
