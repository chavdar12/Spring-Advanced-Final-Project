package bg.softuni.tradezone.dto;

import bg.softuni.tradezone.entity.UserProfile;

public record ChatUser(
    String username, String firstName, String lastName, boolean connected, String photoUrl) {

  public static ChatUser from(UserProfile p) {
    return new ChatUser(
        p.getUsername(), p.getFirstName(), p.getLastName(), p.isConnected(), p.getPhotoUrl());
  }
}
