package bg.softuni.tradezone.web;

import bg.softuni.tradezone.dto.RegionResponse;
import bg.softuni.tradezone.dto.TownResponse;
import bg.softuni.tradezone.dto.TownStats;
import bg.softuni.tradezone.service.RegionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegionController {

  private final RegionService regions;

  @GetMapping("/regions")
  public List<RegionResponse> regions() {
    return regions.regions();
  }

  @GetMapping("/regions/{name}/towns")
  public List<TownStats> townsIn(@PathVariable String name) {
    return regions.townsIn(name);
  }

  @GetMapping("/towns")
  public List<TownResponse> towns() {
    return regions.towns();
  }
}
