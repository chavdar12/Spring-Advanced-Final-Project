package bg.softuni.tradezone.repository;

import bg.softuni.tradezone.model.entity.Role;
import bg.softuni.tradezone.model.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);

    List<Role> findAllByNameNotLike(RoleName name);
}

