package bg.softuni.tradezone.service.impl;

import bg.softuni.tradezone.error.exception.CategoryNotValidException;
import bg.softuni.tradezone.model.entity.Category;
import bg.softuni.tradezone.model.entity.Photo;
import bg.softuni.tradezone.model.entity.UserProfile;
import bg.softuni.tradezone.model.rest.CategoryCreateModel;
import bg.softuni.tradezone.model.service.AdvertisementServiceModel;
import bg.softuni.tradezone.model.service.CategoryServiceModel;
import bg.softuni.tradezone.model.service.PhotoServiceModel;
import bg.softuni.tradezone.repository.CategoryRepository;
import bg.softuni.tradezone.repository.PhotoRepository;
import bg.softuni.tradezone.repository.UserProfileRepository;
import bg.softuni.tradezone.service.base.PhotoService;
import bg.softuni.tradezone.service.validation.CategoryValidationService;
import bg.softuni.tradezone.service.base.CategoryService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static bg.softuni.tradezone.utils.Constants.*;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {


    private final CategoryRepository repository;

    private final UserProfileRepository profileRepository;

    private final PhotoRepository photoRepository;

    private final PhotoService photoService;

    private final CategoryValidationService validationService;

    private final ModelMapper mapper;

    @Override
    @Transactional
    public CategoryServiceModel create(CategoryCreateModel restModel) {

        CategoryServiceModel category = mapper.map(restModel, CategoryServiceModel.class);
        PhotoServiceModel photo = photoService.upload(restModel.getImage());
        category.setPhoto(photo);

        if (!validationService.isValid(category)) {
            throw new CategoryNotValidException(INVALID_DATA_MODEL);
        }

        UserProfile userProfile = profileRepository.findByUserUsername(restModel.getCreator()).orElse(null);

        if (userProfile == null) {
            throw new CategoryNotValidException(INVALID_DATA_MODEL);
        }

        Category entity = mapper.map(category, Category.class);
        entity.setCreator(userProfile);
        entity = repository.save(entity);

        return mapper.map(entity, CategoryServiceModel.class);
    }

    @Override
    public List<CategoryServiceModel> getTop(Integer count) {
        return this.repository.findTopOrderedByAdvertisementsCountDesc(count)
                .stream().map(x -> {
                    CategoryServiceModel serviceModel = this.mapper.map(x, CategoryServiceModel.class);
                    List<AdvertisementServiceModel> advertisements = serviceModel.getAdvertisements();
                    serviceModel.setAdvertisements(advertisements.subList(0, Math.min(advertisements.size(), 10)));
                    return serviceModel;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryServiceModel> getAll() {
        if (repository.count() == 0) {
            seedCategories();
        }
        return repository.findAll().stream()
                .map(x -> mapper.map(x, CategoryServiceModel.class))
                .collect(Collectors.toList());
    }

    private void seedCategories() {
        Photo photo = new Photo();
        photo.setUrl(DEFAULT_CATEGORY_PHOTO_CLOUD_URL);
        photo.setIdInCloud(DEFAULT_CATEGORY_ID_CLOUD);
        photoRepository.save(photo);
        Category category = new Category();
        category.setName(DEFAULT_CATEGORY);
        category.setPhoto(photo);
        repository.save(category);
    }
}
