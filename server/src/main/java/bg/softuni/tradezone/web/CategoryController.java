package bg.softuni.tradezone.web;

import bg.softuni.tradezone.dto.CategoryRequest;
import bg.softuni.tradezone.dto.CategoryResponse;
import bg.softuni.tradezone.dto.TopCategoryResponse;
import bg.softuni.tradezone.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categories;

  @GetMapping
  public List<CategoryResponse> all() {
    return categories.all();
  }

  @GetMapping("/top")
  public List<TopCategoryResponse> top(
      @RequestParam(defaultValue = "3") @Min(1) @Max(10) int count) {
    return categories.top(count);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('MODERATOR')")
  public CategoryResponse create(@Valid @RequestBody CategoryRequest request, Principal principal) {
    return categories.create(principal.getName(), request);
  }
}
