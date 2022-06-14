package bg.softuni.tradezone.service.base;

import bg.softuni.tradezone.model.service.TownServiceModel;

import java.util.List;

public interface TownService {

    List<TownServiceModel> getAllTowns();

    List<TownServiceModel> getAllInRegion(String regionName);
}
