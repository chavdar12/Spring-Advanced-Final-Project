package bg.softuni.tradezone.web;

import bg.softuni.tradezone.dto.PasswordRequest;
import bg.softuni.tradezone.dto.ProfileRequest;
import bg.softuni.tradezone.dto.ProfileResponse;
import bg.softuni.tradezone.service.ProfileService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** The signed-in user's own profile. */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

  private final ProfileService profiles;

  @GetMapping
  public ProfileResponse get(Principal principal) {
    return profiles.get(principal.getName());
  }

  @PatchMapping
  public ProfileResponse update(@Valid @RequestBody ProfileRequest request, Principal principal) {
    return profiles.update(principal.getName(), request);
  }

  @PatchMapping("/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void changePassword(@Valid @RequestBody PasswordRequest request, Principal principal) {
    profiles.changePassword(principal.getName(), request);
  }

  @PutMapping(path = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ProfileResponse changePicture(
      @RequestPart("image") MultipartFile image, Principal principal) throws IOException {
    return profiles.changePicture(principal.getName(), image);
  }

  @PostMapping("/disconnect")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void disconnect(Principal principal) {
    profiles.disconnect(principal.getName());
  }
}
