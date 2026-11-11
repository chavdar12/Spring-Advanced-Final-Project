package bg.softuni.tradezone.repository;

import bg.softuni.tradezone.entity.UserProfile;
import bg.softuni.tradezone.error.ApiException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

  Optional<UserProfile> findByUserUsername(String username);

  List<UserProfile> findAllByIsCompletedTrueAndUserUsernameNot(String username);

  default UserProfile getByUsername(String username) {
    return findByUserUsername(username).orElseThrow(() -> ApiException.notFound("User", username));
  }
}
