package bg.softuni.tradezone.dto;

import bg.softuni.tradezone.entity.Channel;
import bg.softuni.tradezone.entity.Role;
import bg.softuni.tradezone.entity.UserProfile;
import java.util.List;

public record ProfileResponse(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    String aboutMe,
    TownResponse town,
    String photoUrl,
    boolean completed,
    List<String> roles,
    List<String> subscribedTo,
    List<AdvertisementSummary> createdAdvertisements,
    List<AdvertisementSummary> favorites) {

  public static ProfileResponse from(UserProfile p) {
    return new ProfileResponse(
        p.getId(),
        p.getUsername(),
        p.getUser().getEmail(),
        p.getFirstName(),
        p.getLastName(),
        p.getAboutMe(),
        TownResponse.from(p.getTown()),
        p.getPhotoUrl(),
        p.isCompleted(),
        p.getUser().getRoles().stream().map(Role::getAuthority).sorted().toList(),
        p.getSubscribedTo().stream().map(Channel::getId).toList(),
        p.getCreatedAdvertisements().stream().map(AdvertisementSummary::from).toList(),
        p.getFavorites().stream().map(AdvertisementSummary::from).toList());
  }
}
