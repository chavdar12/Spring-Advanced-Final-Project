package bg.softuni.tradezone.model.view;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProfileViewModel {

    private Long id;

    private String firstName;

    private String lastName;

    private String userUsername;

    private TownCompleteProfileViewModel town;

    private String aboutMe;

    private Boolean isCompleted;

    private String photoUrl;

    private String userEmail;

    private List<String> roles;

    private List<CategoryListViewModel> createdCategories;

    private List<String> subscribedTo;

    private List<AdvertisementListViewModel> createdAdvertisements;

    private List<AdvertisementListViewModel> favorites;
}
