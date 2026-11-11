package bg.softuni.tradezone.repository;

import bg.softuni.tradezone.dto.TownStats;
import bg.softuni.tradezone.entity.Town;
import bg.softuni.tradezone.error.ApiException;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TownRepository extends JpaRepository<Town, Long> {

  @EntityGraph(attributePaths = "region")
  List<Town> findAllByOrderByName();

  @Query(
      """
      select new bg.softuni.tradezone.dto.TownStats(t.id, t.name, count(a.id))
      from Town t left join t.citizens p left join p.createdAdvertisements a
      where t.region.name = :region
      group by t.id, t.name order by t.name
      """)
  List<TownStats> findStatsInRegion(String region);

  default Town getRequired(Long id) {
    return findById(id).orElseThrow(() -> ApiException.notFound("Town", id));
  }
}
