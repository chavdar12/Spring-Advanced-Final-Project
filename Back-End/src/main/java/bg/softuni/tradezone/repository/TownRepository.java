package bg.softuni.tradezone.repository;

import bg.softuni.tradezone.model.entity.Town;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TownRepository extends JpaRepository<Town, Long> {

    List<Town> findAllByRegionName(String regionName);
}
