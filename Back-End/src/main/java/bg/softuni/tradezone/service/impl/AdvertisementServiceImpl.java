package bg.softuni.tradezone.service.impl;

import bg.softuni.tradezone.error.exception.*;
import bg.softuni.tradezone.model.entity.Advertisement;
import bg.softuni.tradezone.model.entity.Category;
import bg.softuni.tradezone.model.entity.Photo;
import bg.softuni.tradezone.model.entity.UserProfile;
import bg.softuni.tradezone.model.enums.Condition;
import bg.softuni.tradezone.model.enums.DeliveryType;
import bg.softuni.tradezone.model.rest.*;
import bg.softuni.tradezone.model.rest.search.FullSearchRequest;
import bg.softuni.tradezone.model.rest.search.SearchRequest;
import bg.softuni.tradezone.model.rest.search.SpecificSearch;
import bg.softuni.tradezone.model.service.AdvertisementServiceModel;
import bg.softuni.tradezone.repository.AdvertisementRepository;
import bg.softuni.tradezone.repository.CategoryRepository;
import bg.softuni.tradezone.repository.UserProfileRepository;
import bg.softuni.tradezone.service.base.AdvertisementService;
import bg.softuni.tradezone.service.base.PhotoService;
import bg.softuni.tradezone.service.validation.*;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static bg.softuni.tradezone.utils.Constants.*;

@Service
@AllArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

    private final SpecificSearchValidationService specificSearchValidationService;
    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementValidationService validationService;
    private final FullSearchRequestValidationService fullSearchValidationService;
    private final SearchRequestValidationService searchValidationService;
    private final DeleteAdvRequestValidationService deleteAdvRequestValidationService;
    private final DeleteAdvImageRequestValidationService deleteAdvImageRequestValidationService;
    private final ViewsUpdateValidationService viewsUpdateValidationService;
    private final PhotoService photoService;
    private final ModelMapper modelMapper;
    private final UserProfileRepository userProfileRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public AdvertisementServiceModel getById(Long id) {
        return advertisementRepository.findById(id)
                .map(x -> modelMapper.map(x, AdvertisementServiceModel.class))
                .orElseThrow(() -> new EntityNotFoundException(String.format(ADV_NOT_FOUND, id)));
    }

    @Override
    public Page<AdvertisementServiceModel> getAllByFullSearch(FullSearchRequest request) {

        if (!fullSearchValidationService.isValid(request)) {
            throw new SearchNotValidException(INVALID_SEARCH);
        }

        Integer page = request.getPage();
        String sortBy = request.getSortBy();
        String order = request.getOrder();
        String condition = request.getCondition();
        PageRequest pageRequest = buildPageRequest(page, sortBy, order);

        Page<AdvertisementServiceModel> advertisements;

        if (request.getSearch().equals(UNDEFINED)) {

            BigDecimal min = request.getMin();
            BigDecimal max = request.getMax();
            String category = request.getCategory();

            if (!condition.equals(DEFAULT)) {
                advertisements = getAllByCategoryPriceBetweenAndCondition(request, pageRequest);
            } else {
                advertisements = getAllByCategoryAndPriceBetween(category, min, max, pageRequest);
            }

        } else {

            if (!condition.equals(DEFAULT)) {
                advertisements = getAllByCategoryTitleContainingPriceBetweenAndCondition(request, pageRequest);
            } else {
                advertisements = getAllByCategoryTitleContainingAndPriceBetween(request, pageRequest);
            }
        }

        return advertisements;
    }

    @Override
    public Long getCountBySearch(SearchRequest searchRequest) {

        if (!searchValidationService.isValid(searchRequest)) {
            throw new SearchNotValidException(INVALID_SEARCH);
        }

        String category = searchRequest.getCategory();
        String conditionName = searchRequest.getCondition();
        String search = searchRequest.getSearch();
        BigDecimal min = searchRequest.getMin();
        BigDecimal max = searchRequest.getMax();

        Long count;

        if (category.equals(DEFAULT)) {
            if (conditionName.equals(DEFAULT)) {
                if (search.equals(UNDEFINED)) {
                    count = advertisementRepository.countAdvertisementByPriceBetween(min, max);
                } else {
                    count = advertisementRepository.countByPriceBetweenAndTitleContaining(min, max, search);
                }
            } else {
                Condition condition = Condition.valueOf(conditionName);
                if (search.equals(UNDEFINED)) {
                    count = advertisementRepository.countAdvertisementByPriceBetweenAndCondition(min, max, condition);
                } else {
                    count = advertisementRepository.countByPriceBetweenAndTitleContainingAndCondition(min, max, search, condition);
                }
            }
        } else {
            if (conditionName.equals(DEFAULT)) {
                if (search.equals(UNDEFINED)) {
                    count = advertisementRepository.countAdvertisementByCategoryNameAndPriceBetween(category, min, max);
                } else {
                    count = advertisementRepository.countByPriceBetweenAndTitleContainingAndCategoryName(min, max, search, category);
                }
            } else {
                Condition condition = Condition.valueOf(conditionName);
                if (search.equals(UNDEFINED)) {
                    count = advertisementRepository.countAdvertisementByCategoryNameAndConditionAndPriceBetween(category, condition, min, max);
                } else {
                    count = advertisementRepository.countByPriceBetweenAndTitleContainingAndCategoryNameAndCondition(min, max, search, category, condition);
                }
            }
        }

        return count;
    }

    @Override
    public AdvertisementServiceModel create(AdvertisementCreateModel restModel) {

        if (!validationService.isValid(restModel)) {
            throw new AdvertisementNotValidException(INVALID_MODEL);
        }

        Category category = categoryRepository.findById(restModel.getCategory())
                .orElseThrow(() -> new EntityNotFoundException(String.format(CAT_NOT_FOUND, restModel.getCategory())));

        UserProfile userProfile = userProfileRepository.findByUserUsername(restModel.getCreator())
                .orElseThrow(() -> new EntityNotFoundException(String.format(PROFILE_NOT_FOUND, restModel.getCreator())));

        if (!userProfile.getIsCompleted()) {
            throw new ProfileNotCompletedException(PROFILE_NOT_COMPLETED);
        }

        Advertisement entity = modelMapper.map(restModel, Advertisement.class);
        entity.setViews(0L);
        entity.setCreatedOn(LocalDateTime.now());
        entity.setCategory(category);
        entity.setCreator(userProfile);
        entity.setPhotos(Arrays.stream(restModel.getImages()).map(photoService::upload)
                                 .map(x -> modelMapper.map(x, Photo.class))
                                 .collect(Collectors.toList()));

        entity = advertisementRepository.save(entity);

        return modelMapper.map(entity, AdvertisementServiceModel.class);
    }

    @Override
    public AdvertisementServiceModel edit(AdvertisementEditedModel restModel) {

        if (!validationService.isValid(restModel)) {
            throw new AdvertisementNotValidException(INVALID_MODEL);
        }

        Advertisement advertisement = advertisementRepository.findById(restModel.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format(ADV_NOT_FOUND, restModel.getId())));

        Category category = categoryRepository.findById(restModel.getCategory())
                .orElseThrow(() -> new EntityNotFoundException(String.format(CAT_NOT_FOUND, restModel.getCategory())));

        if (!advertisement.getCreator().getUser().getUsername().equals(restModel.getCreator())) {
            throw new NotAllowedException(NOT_ALLOWED_ACTION);
        }

        if (!advertisement.getCategory().getId().equals(category.getId())) {
            advertisement.setCategory(category);
        }

        advertisement.setTitle(restModel.getTitle());
        advertisement.setDescription(restModel.getDescription());
        advertisement.setPrice(restModel.getPrice());
        advertisement.setCondition(Condition.valueOf(restModel.getCondition()));
        advertisement.setDelivery(DeliveryType.valueOf(restModel.getDelivery()));

        advertisement = advertisementRepository.save(advertisement);

        return modelMapper.map(advertisement, AdvertisementServiceModel.class);
    }

    @Override
    public AdvertisementServiceModel delete(String principalName, DeleteAdvRequest deleteRequest) {

        if (!deleteAdvRequestValidationService.isValid(deleteRequest)) {
            throw new DeleteRequestNotValidException(INVALID_DELETE_REQUEST);
        }

        Advertisement advertisement = advertisementRepository.findById(deleteRequest.getAdvertisementId())
                .orElseThrow(() -> new EntityNotFoundException(String.format(ADV_NOT_FOUND, deleteRequest.getAdvertisementId())));

        if (!principalName.equals(deleteRequest.getUsername())
            || !advertisement.getCreator().getUser().getUsername().equals(principalName)) {

            throw new NotAllowedException(NOT_ALLOWED_ACTION);
        }

        advertisement.getCreator().getCreatedAdvertisements().remove(advertisement);
        advertisement.getProfilesWhichLikedIt().forEach(p -> p.getFavorites().remove(advertisement));
        advertisement.getProfilesWhichViewedIt().forEach(p -> p.getViewed().remove(advertisement));

        advertisement.setProfilesWhichLikedIt(null);
        advertisement.setProfilesWhichViewedIt(null);

        photoService.deleteAll(advertisement.getPhotos().stream().map(Photo::getId).collect(Collectors.toList()));

        advertisementRepository.save(advertisement);
        advertisementRepository.delete(advertisement);

        return modelMapper.map(advertisement, AdvertisementServiceModel.class);
    }

    @Override
    public AdvertisementServiceModel updateViews(Long id, ViewsUpdate update) {

        if (!viewsUpdateValidationService.isValid(update)) {
            throw new ViewsUpdateNotValidException(INVALID_VIEWS_UPDATE);
        }

        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ADV_NOT_FOUND));

        UserProfile profile = userProfileRepository.findByUserUsername(update.getUsername())
                .orElseThrow(() -> new EntityNotFoundException(String.format(PROFILE_NOT_FOUND, update.getUsername())));

        if (profile.getViewed().stream().noneMatch(x -> x.getId().equals(id))) {

            advertisement.setViews(update.getViews());
            advertisementRepository.save(advertisement);

            profile.getViewed().add(advertisement);
            userProfileRepository.save(profile);
        }

        return modelMapper.map(advertisement, AdvertisementServiceModel.class);
    }

    @Override
    public AdvertisementServiceModel deletePhoto(DeleteAdvImageRequest request) {

        if (!deleteAdvImageRequestValidationService.isValid(request)) {
            throw new DeleteRequestNotValidException(INVALID_DELETE_REQUEST);
        }

        Advertisement advertisement = advertisementRepository.findById(request.getAdvertisementId())
                .orElseThrow(() -> new EntityNotFoundException(ADV_NOT_FOUND));

        if (advertisement.getPhotos().stream().noneMatch(x -> x.getId().equals(request.getPhotoId()))) {
            throw new EntityNotFoundException(ADV_IMG_NOT_PRESENT);
        }

        if (!advertisement.getCreator().getUser().getUsername().equals(request.getUsername())) {
            throw new NotAllowedException(NOT_ALLOWED_ACTION);
        }

        advertisement.getPhotos().remove(advertisement.getPhotos().stream()
                                                 .filter(x -> x.getId().equals(request.getPhotoId())).findFirst().get());

        photoService.delete(request.getPhotoId());

        advertisementRepository.save(advertisement);

        return modelMapper.map(advertisement, AdvertisementServiceModel.class);
    }

    @Override
    public List<AdvertisementServiceModel> findByCreatorUsernameExcept(SpecificSearch specificSearch, String username) {

        if (!specificSearchValidationService.isValid(specificSearch)) {
            throw new SearchNotValidException(INVALID_SEARCH);
        }

        PageRequest pageRequest = PageRequest.of(specificSearch.getPage(), 3, Sort.by("views").descending());

        return advertisementRepository
                .findAllByCreatorUserUsernameAndIdNot(username, pageRequest, specificSearch.getExcludeId())
                .stream()
                .map(x -> modelMapper.map(x, AdvertisementServiceModel.class))
                .collect(Collectors.toList());
    }

    private Page<AdvertisementServiceModel> getAllByCategoryTitleContainingPriceBetweenAndCondition(SearchRequest searchRequest, PageRequest pageRequest) {

        String conditionName = searchRequest.getCondition();
        String category = searchRequest.getCategory();
        String search = searchRequest.getSearch();
        BigDecimal min = searchRequest.getMin();
        BigDecimal max = searchRequest.getMax();

        Page<Advertisement> advertisements;

        Condition condition = Condition.valueOf(conditionName);

        if (category.equals(DEFAULT)) {
            advertisements = advertisementRepository.findAllByTitleContainingAndPriceBetweenAndCondition(search, min, max, condition, pageRequest);
        } else {
            advertisements = advertisementRepository.findAllByTitleContainingAndCategoryNameAndPriceBetweenAndCondition(search, category, min, max, condition, pageRequest);
        }

        return new PageImpl<>(advertisements.stream()
                                      .map(x -> modelMapper.map(x, AdvertisementServiceModel.class))
                                      .collect(Collectors.toList()));
    }

    private Page<AdvertisementServiceModel> getAllByCategoryAndPriceBetween(String category, BigDecimal min, BigDecimal max, PageRequest pageRequest) {

        Page<Advertisement> advertisements;

        if (category.equals(DEFAULT)) {
            advertisements = advertisementRepository.findAllByPriceBetween(min, max, pageRequest);
        } else {
            advertisements = advertisementRepository.findAllByCategoryNameAndPriceBetween(category, min, max, pageRequest);
        }

        return new PageImpl<>(advertisements.stream()
                                      .map(x -> modelMapper.map(x, AdvertisementServiceModel.class))
                                      .collect(Collectors.toList()));
    }

    private Page<AdvertisementServiceModel> getAllByCategoryPriceBetweenAndCondition(FullSearchRequest searchRequest, PageRequest pageRequest) {

        String conditionName = searchRequest.getCondition();
        String category = searchRequest.getCategory();
        BigDecimal min = searchRequest.getMin();
        BigDecimal max = searchRequest.getMax();

        Condition condition = Condition.valueOf(conditionName);

        Page<Advertisement> advertisements;

        if (category.equals(DEFAULT)) {
            advertisements = advertisementRepository.findAllByPriceBetweenAndCondition(min, max, condition, pageRequest);
        } else {
            advertisements = advertisementRepository.findAllByCategoryNameAndPriceBetweenAndCondition(category, min, max, condition, pageRequest);
        }

        return new PageImpl<>(advertisements.stream()
                                      .map(x -> modelMapper.map(x, AdvertisementServiceModel.class))
                                      .collect(Collectors.toList()));
    }

    private Page<AdvertisementServiceModel> getAllByCategoryTitleContainingAndPriceBetween(SearchRequest request, PageRequest page) {

        String categoryName = request.getCategory();
        BigDecimal min = request.getMin();
        BigDecimal max = request.getMax();
        String searchText = request.getSearch();

        Page<Advertisement> advertisements;

        if (categoryName.equals(DEFAULT)) {
            advertisements = advertisementRepository.findAllByPriceBetweenAndTitleContaining(min, max, searchText, page);
        } else {
            advertisements = advertisementRepository.findAllByCategoryNameAndTitleContainingAndPriceBetween(categoryName, searchText, min, max, page);
        }

        return new PageImpl<>(advertisements.stream()
                                      .map(x -> modelMapper.map(x, AdvertisementServiceModel.class))
                                      .collect(Collectors.toList()));
    }

    private PageRequest buildPageRequest(Integer pageNumber, String sortBy, String order) {

        PageRequest pageRequest = PageRequest.of(pageNumber - 1, 6);

        if (!sortBy.equals(NONE_SORT) && !order.equals(NONE_SORT)) {

            Sort sort = Sort.by(sortBy);
            if (order.equals(ASCENDING_ORDER)) {
                sort = sort.ascending();
            } else {
                sort = sort.descending();
            }
            pageRequest = PageRequest.of(pageNumber - 1, 6, sort);
        }

        return pageRequest;
    }
}
