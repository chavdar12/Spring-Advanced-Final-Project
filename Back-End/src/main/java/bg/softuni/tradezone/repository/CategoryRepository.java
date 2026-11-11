package bg.softuni.tradezone.repository;

import bg.softuni.tradezone.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findById(Long id);

    @Query(value = "SELECT c.* " +
                   "FROM categories c " +
                   "JOIN advertisements a ON c.id = a.category_id " +
                   "GROUP BY c.id " +
                   "ORDER BY COUNT(a.id) DESC " +
                   "LIMIT :count", nativeQuery = true)
    List<Category> findTopOrderedByAdvertisementsCountDesc(@Param("count") Integer count);

    Boolean existsByName(String categoryName);
}
