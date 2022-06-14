package bg.softuni.tradezone.controller;

import bg.softuni.tradezone.model.view.RegionViewModel;
import bg.softuni.tradezone.service.base.MappingService;
import bg.softuni.tradezone.service.base.RegionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@AllArgsConstructor
@RequestMapping("/api/region")
public class RegionController {

    private final RegionService regionService;
    private final MappingService mappingService;

    @GetMapping("/all")
    public ResponseEntity<List<RegionViewModel>> all() {
        return ResponseEntity.ok(mappingService.mapServiceRegionToView(regionService.getAllRegions()));
    }
}
