package bg.softuni.tradezone.service.base;

import bg.softuni.tradezone.model.service.AdvertisementServiceModel;
import bg.softuni.tradezone.model.service.CategoryServiceModel;
import bg.softuni.tradezone.model.service.ProfileServiceModel;
import bg.softuni.tradezone.model.service.RegionServiceModel;
import bg.softuni.tradezone.model.view.*;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.function.Function;

public interface MappingService {

    ModelMapper getMapper();

    List<AdvertisementListViewModel> mapServiceAdvertisementsToView(List<AdvertisementServiceModel> models);

    List<ProfileTableViewModel> mapServiceToTableViewModel(List<ProfileServiceModel> models, ProfileService service);

    List<RegionViewModel> mapServiceRegionToView(List<RegionServiceModel> models);

    ProfileViewModel mapServiceProfileToView(ProfileServiceModel model);

    Function<CategoryServiceModel, TopCategoryViewModel> serviceToTopCategoryViewModel();
}

