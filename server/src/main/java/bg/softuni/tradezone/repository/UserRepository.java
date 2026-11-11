package bg.softuni.tradezone.repository;

import bg.softuni.tradezone.entity.User;
import bg.softuni.tradezone.error.ApiException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  @EntityGraph(attributePaths = {"profile", "profile.photo"})
  List<User> findAllByOrderById();

  default User getRequired(Long id) {
    return findById(id).orElseThrow(() -> ApiException.notFound("User", id));
  }
}
