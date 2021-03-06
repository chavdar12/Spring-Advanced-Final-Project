package bg.softuni.tradezone.model.service;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserServiceModel extends BaseServiceModel {

    private String username;

    private String email;

    private String password;

    private ProfileServiceModel profile;

    private Set<RoleServiceModel> roles;
}
