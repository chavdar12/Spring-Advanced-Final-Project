package bg.softuni.tradezone.repository;

import bg.softuni.tradezone.entity.Advertisement;
import bg.softuni.tradezone.entity.Category;
import bg.softuni.tradezone.entity.Condition;
import bg.softuni.tradezone.error.ApiException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

  /** A null filter matches everything; {@code titlePattern} is a lowercase LIKE pattern. */
  @Query(
      """
      select a from Advertisement a
      where (:category is null or a.category.name = :category)
        and (:condition is null or a.condition = :condition)
        and (:titlePattern is null or lower(a.title) like :titlePattern)
        and a.price between :min and :max
      """)
  Page<Advertisement> search(
      String titlePattern,
      String category,
      Condition condition,
      BigDecimal min,
      BigDecimal max,
      Pageable pageable);

  List<Advertisement> findAllByCreatorUserUsernameAndIdNot(
      String username, Long excludeId, Pageable pageable);

  List<Advertisement> findAllByCategory(Category category, Pageable pageable);

  default Advertisement getRequired(Long id) {
    return findById(id).orElseThrow(() -> ApiException.notFound("Advertisement", id));
  }
}
