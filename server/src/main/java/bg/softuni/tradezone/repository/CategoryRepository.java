package bg.softuni.tradezone.repository;

import bg.softuni.tradezone.entity.Category;
import bg.softuni.tradezone.error.ApiException;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  boolean existsByName(String name);

  List<Category> findAllByOrderByName();

  @Query("select c from Category c join c.advertisements a group by c order by count(a) desc")
  List<Category> findMostUsed(Pageable pageable);

  default Category getRequired(Long id) {
    return findById(id).orElseThrow(() -> ApiException.notFound("Category", id));
  }
}
