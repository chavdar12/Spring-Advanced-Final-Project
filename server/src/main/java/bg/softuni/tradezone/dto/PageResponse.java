package bg.softuni.tradezone.dto;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

public record PageResponse<T>(List<T> content, long total) {

  public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
    return new PageResponse<>(
        page.getContent().stream().map(mapper).toList(), page.getTotalElements());
  }
}
