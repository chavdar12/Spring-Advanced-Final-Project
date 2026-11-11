package bg.softuni.tradezone.service;

import bg.softuni.tradezone.dto.RegionResponse;
import bg.softuni.tradezone.dto.TownResponse;
import bg.softuni.tradezone.dto.TownStats;
import bg.softuni.tradezone.repository.RegionRepository;
import bg.softuni.tradezone.repository.TownRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Regions and towns are seeded by Flyway and never change; only their counts do. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RegionService {

  private final RegionRepository regions;
  private final TownRepository towns;

  // mutable lists: the Redis JSON serializer can't read back JDK immutable lists
  @Cacheable("regions")
  public List<RegionResponse> regions() {
    return new ArrayList<>(regions.findStats());
  }

  @Cacheable("townsInRegion")
  public List<TownStats> townsIn(String region) {
    return new ArrayList<>(towns.findStatsInRegion(region));
  }

  @Cacheable("towns")
  public List<TownResponse> towns() {
    return towns.findAllByOrderByName().stream()
        .map(TownResponse::from)
        .collect(Collectors.toList());
  }
}
