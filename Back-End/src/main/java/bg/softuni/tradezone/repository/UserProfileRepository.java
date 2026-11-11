package bg.softuni.tradezone.repository;

import bg.softuni.tradezone.model.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserUsername(String username);

    List<UserProfile> findAllByIsCompletedTrue();

    List<UserProfile> findAllByConnectedTrue();
}
