package bg.softuni.tradezone.service.impl;

import bg.softuni.tradezone.error.exception.EntityNotFoundException;
import bg.softuni.tradezone.error.exception.NotAllowedException;
import bg.softuni.tradezone.error.exception.PhotoNotValidException;
import bg.softuni.tradezone.model.entity.Advertisement;
import bg.softuni.tradezone.model.entity.Photo;
import bg.softuni.tradezone.model.rest.ImagesToUploadModel;
import bg.softuni.tradezone.model.rest.message.response.ResponseMessage;
import bg.softuni.tradezone.model.service.PhotoServiceModel;
import bg.softuni.tradezone.repository.AdvertisementRepository;
import bg.softuni.tradezone.repository.PhotoRepository;
import bg.softuni.tradezone.service.base.PhotoService;
import bg.softuni.tradezone.service.validation.PhotoValidationService;
import com.cloudinary.Uploader;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static bg.softuni.tradezone.utils.Constants.*;

@Service
@AllArgsConstructor
public class PhotoServiceImpl implements PhotoService {


    private final PhotoRepository photoRepository;

    private final ModelMapper mapper;

    private final AdvertisementRepository advertisementRepository;

    private final Uploader uploader;

    private final PhotoValidationService validationService;

    @Override
    public PhotoServiceModel upload(String imageContent) {

        if (!validationService.isValid(imageContent)) {
            throw new PhotoNotValidException(FAIL);
        }

        return uploadToCloud(imageContent);
    }

    @Override
    public PhotoServiceModel upload(MultipartFile multipartFile) {
        File file = null;
        try {
            file = File.createTempFile("temp-file", multipartFile.getOriginalFilename());
            multipartFile.transferTo(file);
            return uploadToCloud(file);
        } catch (IOException e) {
            throw new PhotoNotValidException(FAIL);
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    @Override
    public List<PhotoServiceModel> uploadAdvertisementPhotos(ImagesToUploadModel model) {

        if (Arrays.stream(model.getImages()).anyMatch(x -> !validationService.isValid(x))) {
            throw new PhotoNotValidException(FAIL);
        }

        Long id = model.getAdvertisementId();

        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(PHOTO_NOT_FOUND, id)));

        if (!advertisement.getCreator().getUser().getUsername().equals(model.getUsername())) {
            throw new NotAllowedException(NOT_ALLOWED_ACTION);
        }

        List<Photo> photos = new ArrayList<>();

        for (String content : model.getImages()) {
            photos.add(mapper.map(uploadToCloud(content), Photo.class));
        }

        advertisement.getPhotos().addAll(photos);

        advertisement = advertisementRepository.save(advertisement);

        return advertisement.getPhotos().stream()
                .map(x -> mapper.map(x, PhotoServiceModel.class))
                .collect(Collectors.toList());
    }

    @Override
    public ResponseMessage delete(Long id) {

        try {
            Photo photo = photoRepository.findById(id).orElse(null);
            uploader.destroy(photo.getIdInCloud(), new HashMap<>());
            photoRepository.delete(photo);
            return new ResponseMessage(SUCCESS);
        } catch (Exception ignored) {
            return new ResponseMessage(FAIL);
        }
    }

    @Override
    public void deleteAll(List<Long> ids) {

        try {
            List<Photo> photos = photoRepository.findAllById(ids);
            for (Photo photo : photos) {
                uploader.destroy(photo.getIdInCloud(), new HashMap<>());
            }
            photoRepository.deleteAll(photos);
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private PhotoServiceModel uploadToCloud(Object content) {

        Map<String, Object> uploadedData;

        try {
            uploadedData = uploader.upload(content, new HashMap<>());
        } catch (Exception e) {
            throw new PhotoNotValidException(FAIL);
        }

        //cloud works this way
        PhotoServiceModel photoServiceModel = new PhotoServiceModel();
        photoServiceModel.setUrl(String.valueOf(uploadedData.get("url")));
        photoServiceModel.setIdInCloud(String.valueOf(uploadedData.get("public_id")));
        return photoServiceModel;
    }
}
