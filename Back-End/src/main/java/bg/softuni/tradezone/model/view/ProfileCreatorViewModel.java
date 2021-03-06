package bg.softuni.tradezone.model.view;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileCreatorViewModel {

    private Long id;

    private String aboutMe;

    private String userUsername;

    private String firstName;

    private String lastName;

    private String photoUrl;
}
