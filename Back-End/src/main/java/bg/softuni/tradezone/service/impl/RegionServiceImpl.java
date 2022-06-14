package bg.softuni.tradezone.service.impl;

import bg.softuni.tradezone.model.service.RegionServiceModel;
import bg.softuni.tradezone.repository.RegionRepository;
import bg.softuni.tradezone.service.base.SeedingService;
import bg.softuni.tradezone.service.base.RegionService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;
    private final SeedingService seedingService;
    private final ModelMapper modelMapper;

    @Override
    public List<RegionServiceModel> getAllRegions() {
        seedingService.seedIfNeeded();
        return regionRepository.findAll()
                .stream()
                .map(x -> modelMapper.map(x, RegionServiceModel.class))
                .collect(Collectors.toList());
    }
}
