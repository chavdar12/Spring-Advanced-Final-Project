package bg.softuni.tradezone.model.view;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileConversationViewModel {

    private Long id;

    private String userUsername;

    private String firstName;

    private String lastName;

    private Boolean connected;

    private String photoUrl;
}
