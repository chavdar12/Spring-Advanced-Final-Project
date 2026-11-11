package bg.softuni.tradezone.service.base;

import bg.softuni.tradezone.model.rest.*;
import bg.softuni.tradezone.model.rest.search.FullSearchRequest;
import bg.softuni.tradezone.model.rest.search.SearchRequest;
import bg.softuni.tradezone.model.rest.search.SpecificSearch;
import bg.softuni.tradezone.model.service.AdvertisementServiceModel;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdvertisementService {

    Page<AdvertisementServiceModel> getAllByFullSearch(FullSearchRequest search);

    Long getCountBySearch(SearchRequest search);

    AdvertisementServiceModel getById(Long id);

    AdvertisementServiceModel create(AdvertisementCreateModel restModel);

    AdvertisementServiceModel edit(AdvertisementEditedModel restModel);

    AdvertisementServiceModel delete(String principalName, DeleteAdvRequest deleteRequest);

    AdvertisementServiceModel updateViews(Long id, ViewsUpdate views);

    AdvertisementServiceModel deletePhoto(DeleteAdvImageRequest deleteRequest);

    List<AdvertisementServiceModel> findByCreatorUsernameExcept(SpecificSearch specificSearch, String username);
}
