package bg.softuni.tradezone.model.rest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleChange {

    private Long profileId;

    private String newRole;
}
