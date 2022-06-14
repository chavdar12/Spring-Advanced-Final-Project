package bg.softuni.tradezone.service.impl;

import bg.softuni.tradezone.config.security.jwt.JwtProvider;
import bg.softuni.tradezone.error.exception.NotAllowedException;
import bg.softuni.tradezone.model.entity.Role;
import bg.softuni.tradezone.model.entity.User;
import bg.softuni.tradezone.model.entity.UserProfile;
import bg.softuni.tradezone.model.enums.RoleName;
import bg.softuni.tradezone.model.rest.RoleChange;
import bg.softuni.tradezone.model.rest.message.response.JwtResponse;
import bg.softuni.tradezone.model.rest.message.response.ResponseMessage;
import bg.softuni.tradezone.model.service.UserServiceModel;
import bg.softuni.tradezone.model.view.ProfileConversationViewModel;
import bg.softuni.tradezone.repository.RoleRepository;
import bg.softuni.tradezone.repository.UserProfileRepository;
import bg.softuni.tradezone.repository.UserRepository;
import bg.softuni.tradezone.service.base.AuthenticationService;
import bg.softuni.tradezone.service.base.RoleService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.security.Principal;
import java.util.*;

import static bg.softuni.tradezone.utils.Constants.*;

@Service
@AllArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;

    private final UserProfileRepository userProfileRepository;

    private final SimpMessagingTemplate template;

    private final PasswordEncoder encoder;

    private final RoleService roleService;

    private final RoleRepository roleRepository;

    private final ModelMapper mapper;

    private final AuthenticationManager authenticationManager;

    private final JwtProvider jwtProvider;

    @Override
    public ResponseMessage register(UserServiceModel model) {

        if (userRepository.existsByUsername(model.getUsername())) {
            String TAKEN_USERNAME = "Fail -> Username is already taken!";
            return new ResponseMessage(TAKEN_USERNAME);
        }

        if (userRepository.existsByEmail(model.getEmail())) {
            String TAKEN_EMAIL = "Fail -> Email is already in use!";
            return new ResponseMessage(TAKEN_EMAIL);
        }

        if (!roleService.rolesAreSeeded()) {
            roleService.seed();
        }

        User user = mapper.map(model, User.class);
        user.setPassword(encoder.encode(user.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(RoleName.ROLE_USER).orElse(null));

        if (fitsRootRole()) {
            roles.add(roleRepository.findByName(RoleName.ROLE_ROOT).orElse(null));
            roles.add(roleRepository.findByName(RoleName.ROLE_ADMIN).orElse(null));
            roles.add(roleRepository.findByName(RoleName.ROLE_MODERATOR).orElse(null));
        } else if (fitsAdminRole()) {
            roles.add(roleRepository.findByName(RoleName.ROLE_ADMIN).orElse(null));
            roles.add(roleRepository.findByName(RoleName.ROLE_MODERATOR).orElse(null));
        } else if (fitsModeratorRole()) {
            roles.add(roleRepository.findByName(RoleName.ROLE_MODERATOR).orElse(null));
        }

        user.setRoles(roles);

        user = userRepository.save(user);

        UserProfile userProfile = new UserProfile(user);

        userProfile = userProfileRepository.saveAndFlush(userProfile);

        user.setProfile(userProfile);

        userRepository.save(user);

        String REGISTERED_SUCCESSFULLY = "User registered successfully!";
        return new ResponseMessage(REGISTERED_SUCCESSFULLY);
    }

    @Override
    public JwtResponse login(UserServiceModel user) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        connectIfNeeded(userDetails);

        return new JwtResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(NOT_FOUND_USER));
    }

    @Override
    public UserServiceModel changeRole(RoleChange change, Principal principal) {

        UserProfile profile = userProfileRepository.findById(change.getProfileId())
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_USER));

        Role newRole = roleRepository.findByName(RoleName.valueOf(change.getNewRole()))
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_ROLE));

        User user = profile.getUser();

        Set<Role> roles = user.getRoles();

        Role topRole = getTopRole(roles);

        if (tryChangeRoot(topRole, newRole) || nonRootTriesChangeRoleOfAdmin(principal, topRole)) {
            throw new NotAllowedException(NOT_ALLOWED);
        }

        modifyRoles(user, newRole);

        return mapper.map(userRepository.save(user), UserServiceModel.class);
    }

    private boolean nonRootTriesChangeRoleOfAdmin(Principal principal, Role topRole) {
        User requester = userRepository.findByUsername(principal.getName()).orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_USER));
        return topRole.getAuthority().equals(RoleName.ROLE_ADMIN.name()) && !getTopRole(requester.getRoles()).getAuthority().equals(RoleName.ROLE_ROOT.name());
    }

    private Role getTopRole(Set<Role> roles) {
        return roles.stream().filter(x -> x.getAuthority().equals(RoleName.ROLE_ROOT.name())).findFirst()
                .orElse(roles.stream().filter(x -> x.getAuthority().equals(RoleName.ROLE_ADMIN.name())).findFirst()
                                .orElse(roles.stream().filter(x -> x.getAuthority().equals(RoleName.ROLE_MODERATOR.name())).findFirst()
                                                .orElse(roles.stream().filter(x -> x.getAuthority().equals(RoleName.ROLE_USER.name())).findFirst()
                                                                .orElseThrow(() -> new IllegalStateException(MISSING_ROLES)))));
    }

    private void modifyRoles(User user, Role newRole) {

        Deque<Role> stack = initAllRolesInStack();

        while (!stack.peek().getAuthority().equals(newRole.getAuthority())) {
            stack.pop();
        }

        user.setRoles(new HashSet<>(stack));
    }

    private Deque<Role> initAllRolesInStack() {
        List<Role> allRoles = roleRepository.findAll();
        Deque<Role> stack = new ArrayDeque<>();
        stack.push(allRoles.stream().filter(x -> x.getAuthority().equals(RoleName.ROLE_USER.name())).findFirst().orElse(null));
        stack.push(allRoles.stream().filter(x -> x.getAuthority().equals(RoleName.ROLE_MODERATOR.name())).findFirst().orElse(null));
        stack.push(allRoles.stream().filter(x -> x.getAuthority().equals(RoleName.ROLE_ADMIN.name())).findFirst().orElse(null));
        stack.push(allRoles.stream().filter(x -> x.getAuthority().equals(RoleName.ROLE_ROOT.name())).findFirst().orElse(null));
        return stack;
    }

    private boolean tryChangeRoot(Role topRole, Role newRole) {
        return topRole.getAuthority().equals("ROLE_ROOT") || newRole.getAuthority().equals("ROLE_ROOT");
    }

    private void connectIfNeeded(UserDetails user) {

        UserProfile userProfile = userProfileRepository
                .findByUserUsername(user.getUsername())
                .orElseThrow();

        if (userProfile.getConnected()) {
            return;
        }

        userProfile.setConnected(true);

        userProfileRepository.save(userProfile);

        ProfileConversationViewModel viewModel = mapper.map(userProfile, ProfileConversationViewModel.class);

        if (!userProfile.getIsCompleted()) {
            return;
        }

        template.convertAndSend("/channel/login", viewModel);
    }

    private boolean fitsRootRole() {
        return userRepository.count() == 0;
    }

    private boolean fitsAdminRole() {
        return userRepository.count() == 1;
    }

    private boolean fitsModeratorRole() {
        return userRepository.count() == 2;
    }
}
