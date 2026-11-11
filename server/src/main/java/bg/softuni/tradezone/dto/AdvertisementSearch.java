package bg.softuni.tradezone.dto;

import bg.softuni.tradezone.entity.Condition;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/** Query parameters of GET /api/advertisements; every filter is optional. */
public record AdvertisementSearch(
    String search,
    String category,
    Condition condition,
    @PositiveOrZero BigDecimal min,
    @PositiveOrZero BigDecimal max,
    @Min(1) Integer page,
    @Pattern(regexp = "price|views|title|createdOn") String sortBy,
    Sort.Direction order) {

  public static final int PAGE_SIZE = 6;

  public AdvertisementSearch {
    search = search == null || search.isBlank() ? null : search.trim();
    category = category == null || category.isBlank() ? null : category;
    min = min == null ? BigDecimal.ZERO : min;
    max = max == null ? new BigDecimal("10000") : max;
    page = page == null ? 1 : page;
    sortBy = sortBy == null ? "createdOn" : sortBy;
    order = order == null ? Sort.Direction.DESC : order;
  }

  public String titlePattern() {
    return search == null ? null : "%" + search.toLowerCase() + "%";
  }

  public PageRequest pageable() {
    return PageRequest.of(page - 1, PAGE_SIZE, Sort.by(order, sortBy));
  }
}
