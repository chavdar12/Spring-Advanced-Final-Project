package bg.softuni.tradezone.repository;

import bg.softuni.tradezone.dto.RegionResponse;
import bg.softuni.tradezone.entity.Region;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RegionRepository extends JpaRepository<Region, Long> {

  @Query(
      """
      select new bg.softuni.tradezone.dto.RegionResponse(r.name, count(distinct a.id), count(distinct p.id))
      from Region r left join r.towns t left join t.citizens p left join p.createdAdvertisements a
      group by r.name order by r.name
      """)
  List<RegionResponse> findStats();
}
