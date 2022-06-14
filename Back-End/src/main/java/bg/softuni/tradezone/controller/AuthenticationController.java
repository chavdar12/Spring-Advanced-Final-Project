package bg.softuni.tradezone.controller;

import bg.softuni.tradezone.model.rest.LoginForm;
import bg.softuni.tradezone.model.rest.RoleChange;
import bg.softuni.tradezone.model.rest.SignUpForm;
import bg.softuni.tradezone.model.rest.message.response.JwtResponse;
import bg.softuni.tradezone.model.rest.message.response.ResponseMessage;
import bg.softuni.tradezone.model.service.UserServiceModel;
import bg.softuni.tradezone.model.view.ProfileConversationViewModel;
import bg.softuni.tradezone.repository.UserProfileRepository;
import bg.softuni.tradezone.service.base.AuthenticationService;
import bg.softuni.tradezone.service.base.RoleService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.ok;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final ModelMapper modelMapper;
    private final AuthenticationService authenticationService;
    private final UserProfileRepository userProfileRepository;
    private final RoleService roleService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm model) {
        UserServiceModel userServiceModel = modelMapper.map(model, UserServiceModel.class);
        JwtResponse jwtResponse = authenticationService.login(userServiceModel);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm model) {
        UserServiceModel userServiceModel = modelMapper.map(model, UserServiceModel.class);
        ResponseMessage responseMessage = authenticationService.register(userServiceModel);
        HttpStatus status = responseMessage.getMessage().contains("Fail") ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return new ResponseEntity<>(responseMessage, status);
    }

    // move to profile controller
    @GetMapping(value = "/listUsers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<ProfileConversationViewModel> completedProfiles(Principal principal) {
        return userProfileRepository.findAllByIsCompletedTrue()
                .stream()
                .filter(x -> !x.getUser().getUsername().equals(principal.getName()))
                .map(x -> modelMapper.map(x, ProfileConversationViewModel.class))
                .collect(Collectors.toList());
    }

    @PatchMapping("/admin/change-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeRole(@RequestBody RoleChange change, Principal principal) {
        authenticationService.changeRole(change, principal);
        return ok().build();
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String[]> allRoles() {
        return ok(roleService.getAllExceptRoot().stream().map(x -> x.getRoleName().name()).toArray(String[]::new));
    }
}
