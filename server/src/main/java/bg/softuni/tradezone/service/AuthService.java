package bg.softuni.tradezone.service;

import static bg.softuni.tradezone.entity.RoleName.*;

import bg.softuni.tradezone.dto.*;
import bg.softuni.tradezone.entity.Role;
import bg.softuni.tradezone.entity.RoleName;
import bg.softuni.tradezone.entity.User;
import bg.softuni.tradezone.entity.UserProfile;
import bg.softuni.tradezone.error.ApiException;
import bg.softuni.tradezone.repository.RoleRepository;
import bg.softuni.tradezone.repository.UserRepository;
import bg.softuni.tradezone.security.JwtProvider;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository users;
  private final RoleRepository roles;
  private final PasswordEncoder encoder;
  private final AuthenticationManager authenticationManager;
  private final JwtProvider jwt;
  private final SimpMessagingTemplate messaging;

  public void signUp(SignUpRequest request) {
    if (users.existsByUsername(request.username())) {
      throw ApiException.conflict("Username is already taken");
    }
    if (users.existsByEmail(request.email())) {
      throw ApiException.conflict("Email is already in use");
    }
    // the first three accounts run the site
    RoleName role =
        switch ((int) Math.min(users.count(), 3)) {
          case 0 -> ROLE_ROOT;
          case 1 -> ROLE_ADMIN;
          case 2 -> ROLE_MODERATOR;
          default -> ROLE_USER;
        };
    User user = new User(request.username(), request.email(), encoder.encode(request.password()));
    user.setRoles(rolesUpTo(role));
    users.save(user);
  }

  public AuthResponse signIn(LoginRequest request) {
    authenticationManager.authenticate(
        UsernamePasswordAuthenticationToken.unauthenticated(
            request.username(), request.password()));
    User user = users.findByUsername(request.username()).orElseThrow();
    UserProfile profile = user.getProfile();
    if (!profile.isConnected()) {
      profile.setConnected(true);
      if (profile.isCompleted()) {
        messaging.convertAndSend("/channel/login", ChatUser.from(profile));
      }
    }
    List<String> authorities = user.getRoles().stream().map(Role::getAuthority).sorted().toList();
    return new AuthResponse(jwt.generate(user.getUsername()), user.getUsername(), authorities);
  }

  @Transactional(readOnly = true)
  public List<AdminUserResponse> allUsers() {
    return users.findAllByOrderById().stream().map(AdminUserResponse::from).toList();
  }

  public AdminUserResponse changeRole(Long userId, RoleName role, String requester) {
    User user = users.getRequired(userId);
    RoleName current = user.topRole();
    if (current == ROLE_ROOT || role == ROLE_ROOT) {
      throw ApiException.forbidden("The root role can't be granted or taken away");
    }
    if (current == ROLE_ADMIN
        && users.findByUsername(requester).orElseThrow().topRole() != ROLE_ROOT) {
      throw ApiException.forbidden("Only root can change an admin's role");
    }
    user.setRoles(rolesUpTo(role));
    return AdminUserResponse.from(user);
  }

  /** A role includes every role below it. */
  private Set<Role> rolesUpTo(RoleName top) {
    return roles.findAll().stream()
        .filter(r -> r.getName().compareTo(top) <= 0)
        .collect(Collectors.toCollection(HashSet::new));
  }
}
