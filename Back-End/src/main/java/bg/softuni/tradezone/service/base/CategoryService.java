package bg.softuni.tradezone.service.base;

import bg.softuni.tradezone.model.rest.CategoryCreateModel;
import bg.softuni.tradezone.model.service.CategoryServiceModel;

import java.util.List;

public interface CategoryService {

    CategoryServiceModel create(CategoryCreateModel createModel);

    List<CategoryServiceModel> getTop(Integer count);

    List<CategoryServiceModel> getAll();
}
