package bg.softuni.tradezone.service.impl;

import bg.softuni.tradezone.model.service.TownServiceModel;
import bg.softuni.tradezone.repository.TownRepository;
import bg.softuni.tradezone.service.base.SeedingService;
import bg.softuni.tradezone.service.base.TownService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TownServiceImpl implements TownService {

    private final TownRepository townRepository;
    private final SeedingService seedingService;
    private final ModelMapper modelMapper;

    @Override
    public List<TownServiceModel> getAllTowns() {
        seedingService.seedIfNeeded();
        return townRepository.findAll()
                .stream()
                .map(x -> modelMapper.map(x, TownServiceModel.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<TownServiceModel> getAllInRegion(String regionName) {
        seedingService.seedIfNeeded();
        return townRepository.findAllByRegionName(regionName).stream()
                .map(x -> modelMapper.map(x, TownServiceModel.class))
                .collect(Collectors.toList());
    }
}
