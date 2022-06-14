package bg.softuni.tradezone.service.impl;

import bg.softuni.tradezone.error.exception.NotAllowedException;
import bg.softuni.tradezone.error.exception.PasswordUpdateNotValidException;
import bg.softuni.tradezone.error.exception.ProfileUpdateNotValidException;
import bg.softuni.tradezone.model.entity.Advertisement;
import bg.softuni.tradezone.model.entity.Photo;
import bg.softuni.tradezone.model.entity.Town;
import bg.softuni.tradezone.model.entity.UserProfile;
import bg.softuni.tradezone.model.rest.PasswordUpdate;
import bg.softuni.tradezone.model.rest.ProfileUpdate;
import bg.softuni.tradezone.model.service.PhotoServiceModel;
import bg.softuni.tradezone.model.service.ProfileServiceModel;
import bg.softuni.tradezone.model.service.RoleServiceModel;
import bg.softuni.tradezone.model.view.ProfileConversationViewModel;
import bg.softuni.tradezone.repository.AdvertisementRepository;
import bg.softuni.tradezone.repository.TownRepository;
import bg.softuni.tradezone.repository.UserProfileRepository;
import bg.softuni.tradezone.service.base.PhotoService;
import bg.softuni.tradezone.service.base.ProfileService;
import bg.softuni.tradezone.service.validation.ProfileUpdateValidationService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static bg.softuni.tradezone.utils.Constants.*;

@Service
@AllArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final SimpMessagingTemplate template;
    private final ProfileUpdateValidationService profileUpdateValidationService;
    private final TownRepository townRepository;
    private final PhotoService photoService;
    private final UserProfileRepository userProfileRepository;
    private final AdvertisementRepository advertisementRepository;
    private final BCryptPasswordEncoder encoder;
    private final ModelMapper mapper;

    @Override
    public ProfileServiceModel getUserProfileByUsername(String username) {

        UserProfile profile = userProfileRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format(PROFILE_NOT_FOUND, username)));

        return mapper.map(profile, ProfileServiceModel.class);
    }

    @Override
    public ProfileServiceModel disconnect(String username) {

        UserProfile userProfile = userProfileRepository
                .findByUserUsername(username)
                .orElseThrow();

        if (!userProfile.getConnected()) {
            return null;
        }

        userProfile.setConnected(false);
        userProfile.setSubscribedTo(new ArrayList<>());

        userProfile = userProfileRepository.save(userProfile);

        ProfileConversationViewModel viewModel = mapper.map(userProfile, ProfileConversationViewModel.class);

        template.convertAndSend("/channel/logout", viewModel);

        return mapper.map(userProfile, ProfileServiceModel.class);
    }

    @Override
    public Boolean isCompleted(String username) {

        return userProfileRepository.findByUserUsername(username)
                .orElseThrow((EntityNotFoundException::new))
                .getIsCompleted();
    }

    @Override
    public String getTopRole(Set<RoleServiceModel> roles) {
        String role = "User";

        if (roles.stream().anyMatch(x -> x.getRoleName().name().equals("ROLE_ROOT"))) {
            role = "ROOT";
        } else if (roles.stream().anyMatch(x -> x.getRoleName().name().equals("ROLE_ADMIN"))) {
            role = "Admin";
        } else if (roles.stream().anyMatch(x -> x.getRoleName().name().equals("ROLE_MODERATOR"))) {
            role = "Moderator";
        }

        return role;
    }

    @Override
    public List<ProfileServiceModel> getAll() {
        return this.userProfileRepository.findAll().stream()
                .map(x -> mapper.map(x, ProfileServiceModel.class))
                .collect(Collectors.toList());
    }

    @Override
    public ProfileServiceModel update(ProfileUpdate update) {

        if (!profileUpdateValidationService.isValid(update)) {
            throw new ProfileUpdateNotValidException(INVALID_UPDATE);
        }

        UserProfile profile = userProfileRepository.findById(update.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format(PROFILE_NOT_FOUND_ID, update.getId())));

        Long townId = update.getTown();

        Town town = townRepository.findById(townId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(TOWN_NOT_FOUND, townId)));

        profile.setFirstName(update.getFirstName());
        profile.setLastName(update.getLastName());
        profile.setTown(town);
        profile.setAboutMe(update.getAboutMe());

        profile = userProfileRepository.save(profile);

        return mapper.map(profile, ProfileServiceModel.class);
    }

    @Override
    public ProfileServiceModel updatePicture(String username, MultipartFile file) {

        UserProfile profile = userProfileRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format(PROFILE_NOT_FOUND, username)));

        Photo photo = profile.getPhoto();

        PhotoServiceModel photoServiceModel = photoService.upload(file);
        Photo newPhoto = mapper.map(photoServiceModel, Photo.class);
        profile.setPhoto(newPhoto);

        if (!profile.getIsCompleted() && profile.getFirstName() != null) {
            profile.setIsCompleted(true);

            ProfileConversationViewModel viewModel = mapper.map(profile, ProfileConversationViewModel.class);
            template.convertAndSend("/channel/login", viewModel);
        }

        profile = userProfileRepository.save(profile);

        if (photo != null) {
            photoService.delete(photo.getId());
        }

        return mapper.map(profile, ProfileServiceModel.class);
    }

    @Override
    public ProfileServiceModel addFavorite(String username, Long addId) {

        UserProfile userProfile = userProfileRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format(PROFILE_NOT_FOUND, username)));

        Advertisement advertisement = advertisementRepository.findById(addId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ADD_NOT_FOUND, addId)));

        if (profileAlreadyLikedTheAdvertisement(userProfile, advertisement)) {
            throw new NotAllowedException(CANT_LIKE);
        }

        userProfile.getFavorites().add(advertisement);
        userProfile = userProfileRepository.save(userProfile);

        return mapper.map(userProfile, ProfileServiceModel.class);
    }

    @Override
    public ProfileServiceModel removeFavorite(String username, Long addId) {

        UserProfile profile = userProfileRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format(PROFILE_NOT_FOUND, username)));

        Advertisement advertisement = advertisementRepository.findById(addId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(ADD_NOT_FOUND, addId)));

        if (profileFavoritesDoesntContainTheAdvertisement(profile, advertisement)) {
            throw new NotAllowedException(IMPOSSIBLE);
        }

        profile.getFavorites().remove(advertisement);
        profile = userProfileRepository.save(profile);

        return mapper.map(profile, ProfileServiceModel.class);
    }

    @Override
    public ProfileServiceModel updatePassword(PasswordUpdate update) {

        UserProfile profile = userProfileRepository.findById(update.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format(PROFILE_NOT_FOUND_ID, update.getId())));

        if (!update.getNewPassword().equals(update.getConfirmNewPassword())) {
            throw new PasswordUpdateNotValidException(PASSWORDS_DOESNT_MATCH);
        }

        if (!encoder.matches(update.getOldPassword(), profile.getUser().getPassword())) {
            throw new NotAllowedException(WRONG_OLD_PASSWORD);
        }

        profile.getUser().setPassword(encoder.encode(update.getNewPassword()));

        profile = userProfileRepository.save(profile);

        return mapper.map(profile, ProfileServiceModel.class);
    }

    private boolean profileAlreadyLikedTheAdvertisement(UserProfile profile, Advertisement advertisement) {
        return profile.getFavorites().stream().anyMatch(x -> x.getId().equals(advertisement.getId()));
    }

    private boolean profileFavoritesDoesntContainTheAdvertisement(UserProfile profile, Advertisement advertisement) {
        return profile.getFavorites().stream().noneMatch(x -> x.getId().equals(advertisement.getId()));
    }
}
