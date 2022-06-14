package bg.softuni.tradezone.controller;

import bg.softuni.tradezone.model.rest.CategoryCreateModel;
import bg.softuni.tradezone.model.view.CategoryListViewModel;
import bg.softuni.tradezone.model.view.CategorySelectViewModel;
import bg.softuni.tradezone.model.view.TopCategoryViewModel;
import bg.softuni.tradezone.service.base.CategoryService;
import bg.softuni.tradezone.service.base.MappingService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping("/api/category")
@AllArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final MappingService mappingService;

    @GetMapping("/all")
    public ResponseEntity<List<CategoryListViewModel>> categories() {
        return ok(categoryService.getAll().stream()
                          .map(x -> mappingService.getMapper().map(x, CategoryListViewModel.class))
                          .collect(Collectors.toList()));
    }

    @GetMapping("/top")
    public ResponseEntity<List<TopCategoryViewModel>> topCategories(@RequestParam(name = "count") Integer count) {
        return ok(categoryService.getTop(count)
                          .stream()
                          .map(mappingService.serviceToTopCategoryViewModel())
                          .collect(Collectors.toList()));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Void> create(@RequestBody CategoryCreateModel restModel) {
        categoryService.create(restModel);
        return ok().build();
    }

    @GetMapping("/select")
    public ResponseEntity<List<CategorySelectViewModel>> categorySelect() {
        return ok(categoryService.getAll().stream()
                          .map(x -> mappingService.getMapper().map(x, CategorySelectViewModel.class))
                          .collect(Collectors.toList()));
    }
}
