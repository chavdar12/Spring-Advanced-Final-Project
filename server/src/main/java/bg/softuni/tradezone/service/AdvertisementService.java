package bg.softuni.tradezone.service;

import bg.softuni.tradezone.dto.*;
import bg.softuni.tradezone.entity.Advertisement;
import bg.softuni.tradezone.entity.Photo;
import bg.softuni.tradezone.entity.UserProfile;
import bg.softuni.tradezone.error.ApiException;
import bg.softuni.tradezone.repository.AdvertisementRepository;
import bg.softuni.tradezone.repository.CategoryRepository;
import bg.softuni.tradezone.repository.UserProfileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdvertisementService {

  private static final int MAX_PHOTOS = 10;
  private static final int BY_CREATOR_PAGE_SIZE = 3;

  private final AdvertisementRepository advertisements;
  private final CategoryRepository categories;
  private final UserProfileRepository profiles;
  private final PhotoService photos;

  @Transactional(readOnly = true)
  public PageResponse<AdvertisementSummary> search(AdvertisementSearch q) {
    return PageResponse.from(
        advertisements.search(
            q.titlePattern(), q.category(), q.condition(), q.min(), q.max(), q.pageable()),
        AdvertisementSummary::from);
  }

  @Transactional(readOnly = true)
  public AdvertisementDetails get(Long id) {
    return AdvertisementDetails.from(advertisements.getRequired(id));
  }

  @Transactional(readOnly = true)
  public List<AdvertisementSummary> byCreator(String creator, Long excludeId, int page) {
    PageRequest pageable =
        PageRequest.of(page, BY_CREATOR_PAGE_SIZE, Sort.by("views").descending());
    return advertisements
        .findAllByCreatorUserUsernameAndIdNot(creator, excludeId, pageable)
        .stream()
        .map(AdvertisementSummary::from)
        .toList();
  }

  public AdvertisementDetails create(String username, AdvertisementRequest request) {
    UserProfile creator = profiles.getByUsername(username);
    if (!creator.isCompleted()) {
      throw ApiException.forbidden("Complete your profile before publishing advertisements");
    }
    Advertisement ad = new Advertisement();
    ad.setCreator(creator);
    apply(ad, request);
    request.images().forEach(image -> ad.getPhotos().add(photos.upload(image)));
    return AdvertisementDetails.from(advertisements.save(ad));
  }

  public AdvertisementDetails update(String username, Long id, AdvertisementRequest request) {
    Advertisement ad = owned(username, id);
    apply(ad, request);
    return AdvertisementDetails.from(ad);
  }

  public void delete(String username, Long id) {
    Advertisement ad = owned(username, id);
    ad.getPhotos().forEach(photos::delete);
    advertisements.delete(ad);
  }

  public AdvertisementDetails addPhotos(String username, Long id, List<String> images) {
    Advertisement ad = owned(username, id);
    if (ad.getPhotos().size() + images.size() > MAX_PHOTOS) {
      throw ApiException.badRequest(
          "An advertisement can have at most %d photos".formatted(MAX_PHOTOS));
    }
    images.forEach(image -> ad.getPhotos().add(photos.upload(image)));
    return AdvertisementDetails.from(ad);
  }

  public void deletePhoto(String username, Long id, Long photoId) {
    Advertisement ad = owned(username, id);
    Photo photo =
        ad.getPhotos().stream()
            .filter(p -> p.getId().equals(photoId))
            .findFirst()
            .orElseThrow(() -> ApiException.notFound("Photo", photoId));
    ad.getPhotos().remove(photo);
    photos.delete(photo);
  }

  /** Counts one view per user. */
  public void view(String username, Long id) {
    Advertisement ad = advertisements.getRequired(id);
    if (profiles.getByUsername(username).getViewed().add(ad)) {
      ad.setViews(ad.getViews() + 1);
    }
  }

  public void favorite(String username, Long id, boolean liked) {
    Advertisement ad = advertisements.getRequired(id);
    UserProfile profile = profiles.getByUsername(username);
    if (liked) {
      profile.getFavorites().add(ad);
    } else {
      profile.getFavorites().remove(ad);
    }
  }

  private Advertisement owned(String username, Long id) {
    Advertisement ad = advertisements.getRequired(id);
    if (!ad.getCreator().getUsername().equals(username)) {
      throw ApiException.forbidden("Only the creator can change this advertisement");
    }
    return ad;
  }

  private void apply(Advertisement ad, AdvertisementRequest request) {
    ad.setTitle(request.title());
    ad.setDescription(request.description());
    ad.setPrice(request.price());
    ad.setCondition(request.condition());
    ad.setDelivery(request.delivery());
    ad.setCategory(categories.getRequired(request.categoryId()));
  }
}
