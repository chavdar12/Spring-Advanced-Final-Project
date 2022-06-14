package bg.softuni.tradezone.controller;

import bg.softuni.tradezone.model.enums.Condition;
import bg.softuni.tradezone.model.enums.DeliveryType;
import bg.softuni.tradezone.model.rest.*;
import bg.softuni.tradezone.model.rest.search.FullSearchRequest;
import bg.softuni.tradezone.model.rest.search.SearchRequest;
import bg.softuni.tradezone.model.rest.search.SpecificSearch;
import bg.softuni.tradezone.model.service.AdvertisementServiceModel;
import bg.softuni.tradezone.model.view.AdvertisementDetailsViewModel;
import bg.softuni.tradezone.model.view.AdvertisementListViewModel;
import bg.softuni.tradezone.model.view.AdvertisementToEditViewModel;
import bg.softuni.tradezone.model.view.PhotoViewModel;
import bg.softuni.tradezone.service.base.AdvertisementService;
import bg.softuni.tradezone.service.base.MappingService;
import bg.softuni.tradezone.service.base.PhotoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowedHeaders = "*")
@RequestMapping("/api")
@AllArgsConstructor
@PreAuthorize("hasRole('USER')")
public class AdvertisementController {

    private final AdvertisementService advertisementService;
    private final PhotoService photoService;
    private final MappingService mappingService;

    @GetMapping("/search")
    public ResponseEntity<List<AdvertisementListViewModel>> filterByCategoryAndTitle(FullSearchRequest search) {
        List<AdvertisementServiceModel> advertisements = advertisementService.getAllByFullSearch(search).getContent();
        return ok(mappingService.mapServiceAdvertisementsToView(advertisements));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countByAll(SearchRequest params) {
        return ok(advertisementService.getCountBySearch(params));
    }

    @GetMapping("/created-by/{username}")
    public ResponseEntity<List<AdvertisementListViewModel>> getByCreator(SpecificSearch specificSearch, @PathVariable String username) {
        List<AdvertisementServiceModel> serviceModels = advertisementService.findByCreatorUsernameExcept(specificSearch, username);
        List<AdvertisementListViewModel> viewModels = mappingService.mapServiceAdvertisementsToView(serviceModels);
        return ok(viewModels);
    }

    @PostMapping("/create")
    public ResponseEntity<Void> create(@RequestBody AdvertisementCreateModel restModel) {
        advertisementService.create(restModel);
        return ok().build();
    }

    @GetMapping("/edit/{id}")
    public ResponseEntity<AdvertisementToEditViewModel> edit(@PathVariable Long id) {
        AdvertisementServiceModel serviceModel = advertisementService.getById(id);
        AdvertisementToEditViewModel viewModel = mappingService.getMapper().map(serviceModel, AdvertisementToEditViewModel.class);
        viewModel.setCreator(serviceModel.getCreator().getUser().getUsername());
        return ok(viewModel);
    }

    @PutMapping("/edit")
    public ResponseEntity<Void> editConfirm(@RequestBody AdvertisementEditedModel editedModel) {
        advertisementService.edit(editedModel);
        return ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(Principal principal, DeleteAdvRequest deleteRequest) {
        String principalName = principal.getName();
        advertisementService.delete(principalName, deleteRequest);
        return ok().build();
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<AdvertisementDetailsViewModel> details(@PathVariable Long id) {
        AdvertisementServiceModel serviceModel = advertisementService.getById(id);
        AdvertisementDetailsViewModel viewModel = mappingService.getMapper().map(serviceModel, AdvertisementDetailsViewModel.class);
        viewModel.setImages(mappingService.getMapper().map(serviceModel.getPhotos(), PhotoViewModel[].class));
        return ok(viewModel);
    }

    @GetMapping("/conditions/all")
    public ResponseEntity<String[]> conditions() {
        return ok(Arrays.stream(Condition.values()).map(Enum::name).toArray(String[]::new));
    }

    @GetMapping("/deliveries/all")
    public ResponseEntity<String[]> deliveries() {
        return ok(Arrays.stream(DeliveryType.values()).map(Enum::name).toArray(String[]::new));
    }

    @PatchMapping("/increase-views/{id}")
    public ResponseEntity<Void> updateViews(@PathVariable Long id, @RequestBody ViewsUpdate update) {
        advertisementService.updateViews(id, update);
        return ok().build();
    }

    @DeleteMapping("/delete-image")
    public ResponseEntity<Void> deletePhoto(DeleteAdvImageRequest deleteRequest) {
        advertisementService.deletePhoto(deleteRequest);
        return ok().build();
    }

    @PatchMapping("/upload-images")
    public ResponseEntity<Void> uploadPhotos(@RequestBody ImagesToUploadModel images) {
        photoService.uploadAdvertisementPhotos(images);
        return ok().build();
    }
}
