package bg.softuni.tradezone.service.base;

import bg.softuni.tradezone.model.rest.RoleChange;
import bg.softuni.tradezone.model.rest.message.response.JwtResponse;
import bg.softuni.tradezone.model.rest.message.response.ResponseMessage;
import bg.softuni.tradezone.model.service.UserServiceModel;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.security.Principal;

public interface AuthenticationService extends UserDetailsService {

    ResponseMessage register(UserServiceModel user);

    JwtResponse login(UserServiceModel user);

    UserServiceModel changeRole(RoleChange change, Principal principal);
}
