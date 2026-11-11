package bg.softuni.tradezone.service;

import bg.softuni.tradezone.dto.ChatUser;
import bg.softuni.tradezone.dto.PasswordRequest;
import bg.softuni.tradezone.dto.ProfileRequest;
import bg.softuni.tradezone.dto.ProfileResponse;
import bg.softuni.tradezone.entity.Photo;
import bg.softuni.tradezone.entity.UserProfile;
import bg.softuni.tradezone.error.ApiException;
import bg.softuni.tradezone.repository.TownRepository;
import bg.softuni.tradezone.repository.UserProfileRepository;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class ProfileService {

  private static final Set<String> IMAGE_TYPES = Set.of("image/png", "image/jpeg");

  private final UserProfileRepository profiles;
  private final TownRepository towns;
  private final PhotoService photos;
  private final PasswordEncoder encoder;
  private final SimpMessagingTemplate messaging;

  @Transactional(readOnly = true)
  public ProfileResponse get(String username) {
    return ProfileResponse.from(profiles.getByUsername(username));
  }

  public ProfileResponse update(String username, ProfileRequest request) {
    UserProfile profile = profiles.getByUsername(username);
    profile.setFirstName(request.firstName());
    profile.setLastName(request.lastName());
    profile.setAboutMe(request.aboutMe());
    profile.setTown(towns.getRequired(request.townId()));
    completeIfReady(profile);
    return ProfileResponse.from(profile);
  }

  public void changePassword(String username, PasswordRequest request) {
    UserProfile profile = profiles.getByUsername(username);
    if (!encoder.matches(request.oldPassword(), profile.getUser().getPassword())) {
      throw ApiException.badRequest("The current password is wrong");
    }
    profile.getUser().setPassword(encoder.encode(request.newPassword()));
  }

  public ProfileResponse changePicture(String username, MultipartFile file) throws IOException {
    if (file.isEmpty() || !IMAGE_TYPES.contains(file.getContentType())) {
      throw ApiException.badRequest("The picture must be a PNG or JPEG image");
    }
    UserProfile profile = profiles.getByUsername(username);
    Photo old = profile.getPhoto();
    profile.setPhoto(photos.upload(file.getBytes()));
    if (old != null) {
      photos.delete(old);
    }
    completeIfReady(profile);
    return ProfileResponse.from(profile);
  }

  public void disconnect(String username) {
    UserProfile profile = profiles.getByUsername(username);
    if (profile.isConnected()) {
      profile.setConnected(false);
      profile.getSubscribedTo().clear();
      messaging.convertAndSend("/channel/logout", ChatUser.from(profile));
    }
  }

  /** Announces the user to the chat the moment their profile becomes complete. */
  private void completeIfReady(UserProfile profile) {
    boolean wasCompleted = profile.isCompleted();
    profile.completeIfReady();
    if (!wasCompleted && profile.isCompleted()) {
      messaging.convertAndSend("/channel/login", ChatUser.from(profile));
    }
  }
}
