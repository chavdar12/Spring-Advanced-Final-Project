package bg.softuni.tradezone.service.base;

import bg.softuni.tradezone.model.rest.PasswordUpdate;
import bg.softuni.tradezone.model.rest.ProfileUpdate;
import bg.softuni.tradezone.model.service.ProfileServiceModel;
import bg.softuni.tradezone.model.service.RoleServiceModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface ProfileService {

    ProfileServiceModel getUserProfileByUsername(String username);

    ProfileServiceModel disconnect(String username);

    Boolean isCompleted(String username);

    String getTopRole(Set<RoleServiceModel> roles);

    List<ProfileServiceModel> getAll();

    ProfileServiceModel update(ProfileUpdate update);

    ProfileServiceModel updatePicture(String username, MultipartFile file);

    ProfileServiceModel addFavorite(String username, Long addId);

    ProfileServiceModel removeFavorite(String username, Long addId);

    ProfileServiceModel updatePassword(PasswordUpdate update);
}
