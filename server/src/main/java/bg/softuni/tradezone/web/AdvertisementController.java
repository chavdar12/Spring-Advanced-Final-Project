package bg.softuni.tradezone.web;

import bg.softuni.tradezone.dto.*;
import bg.softuni.tradezone.service.AdvertisementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.groups.Default;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {

  private final AdvertisementService advertisements;

  @GetMapping
  public PageResponse<AdvertisementSummary> search(@Valid AdvertisementSearch search) {
    return advertisements.search(search);
  }

  @GetMapping("/options")
  public AdvertisementOptions options() {
    return AdvertisementOptions.ALL;
  }

  @GetMapping("/{id}")
  public AdvertisementDetails get(@PathVariable Long id) {
    return advertisements.get(id);
  }

  @GetMapping("/by/{username}")
  public List<AdvertisementSummary> byCreator(
      @PathVariable String username,
      @RequestParam Long excludeId,
      @RequestParam(defaultValue = "0") @PositiveOrZero int page) {
    return advertisements.byCreator(username, excludeId, page);
  }

  @PostMapping
  public ResponseEntity<AdvertisementDetails> create(
      @Validated({Default.class, AdvertisementRequest.OnCreate.class}) @RequestBody
          AdvertisementRequest request,
      Principal principal) {
    AdvertisementDetails created = advertisements.create(principal.getName(), request);
    return ResponseEntity.created(URI.create("/api/advertisements/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  public AdvertisementDetails update(
      @PathVariable Long id,
      @Valid @RequestBody AdvertisementRequest request,
      Principal principal) {
    return advertisements.update(principal.getName(), id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, Principal principal) {
    advertisements.delete(principal.getName(), id);
  }

  @PostMapping("/{id}/photos")
  public AdvertisementDetails addPhotos(
      @PathVariable Long id, @Valid @RequestBody ImagesRequest request, Principal principal) {
    return advertisements.addPhotos(principal.getName(), id, request.images());
  }

  @DeleteMapping("/{id}/photos/{photoId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletePhoto(@PathVariable Long id, @PathVariable Long photoId, Principal principal) {
    advertisements.deletePhoto(principal.getName(), id, photoId);
  }

  @PostMapping("/{id}/views")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void view(@PathVariable Long id, Principal principal) {
    advertisements.view(principal.getName(), id);
  }

  @PostMapping("/{id}/favorite")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void like(@PathVariable Long id, Principal principal) {
    advertisements.favorite(principal.getName(), id, true);
  }

  @DeleteMapping("/{id}/favorite")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unlike(@PathVariable Long id, Principal principal) {
    advertisements.favorite(principal.getName(), id, false);
  }
}
