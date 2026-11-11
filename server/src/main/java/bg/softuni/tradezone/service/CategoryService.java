package bg.softuni.tradezone.service;

import bg.softuni.tradezone.dto.AdvertisementSummary;
import bg.softuni.tradezone.dto.CategoryRequest;
import bg.softuni.tradezone.dto.CategoryResponse;
import bg.softuni.tradezone.dto.TopCategoryResponse;
import bg.softuni.tradezone.entity.Category;
import bg.softuni.tradezone.error.ApiException;
import bg.softuni.tradezone.repository.AdvertisementRepository;
import bg.softuni.tradezone.repository.CategoryRepository;
import bg.softuni.tradezone.repository.UserProfileRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

  private static final int TOP_ADS_PER_CATEGORY = 10;

  private final CategoryRepository categories;
  private final AdvertisementRepository advertisements;
  private final UserProfileRepository profiles;
  private final PhotoService photos;

  // Collectors.toList(): the Redis JSON serializer can't read back JDK immutable lists
  @Cacheable("categories")
  public List<CategoryResponse> all() {
    return categories.findAllByOrderByName().stream()
        .map(CategoryResponse::from)
        .collect(Collectors.toList());
  }

  public List<TopCategoryResponse> top(int count) {
    return categories.findMostUsed(PageRequest.ofSize(count)).stream()
        .map(c ->
            new TopCategoryResponse(
                c.getName(),
                c.getPhoto().getUrl(),
                advertisements
                    .findAllByCategory(c, PageRequest.ofSize(TOP_ADS_PER_CATEGORY))
                    .stream()
                    .map(AdvertisementSummary::from)
                    .toList()))
        .toList();
  }

  @Transactional
  @CacheEvict(cacheNames = "categories", allEntries = true)
  public CategoryResponse create(String username, CategoryRequest request) {
    if (categories.existsByName(request.name())) {
      throw ApiException.conflict("Category '%s' already exists".formatted(request.name()));
    }
    Category category =
        new Category(
            request.name(), photos.upload(request.image()), profiles.getByUsername(username));
    return CategoryResponse.from(categories.save(category));
  }
}
