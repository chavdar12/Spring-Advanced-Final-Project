package bg.softuni.tradezone.web;

import bg.softuni.tradezone.dto.AdminUserResponse;
import bg.softuni.tradezone.dto.RoleRequest;
import bg.softuni.tradezone.entity.RoleName;
import bg.softuni.tradezone.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

  private final AuthService auth;

  @GetMapping("/users")
  public List<AdminUserResponse> users() {
    return auth.allUsers();
  }

  @GetMapping("/roles")
  public List<RoleName> roles() {
    return List.of(RoleName.values());
  }

  @PatchMapping("/users/{id}/role")
  public AdminUserResponse changeRole(
      @PathVariable Long id, @Valid @RequestBody RoleRequest request, Principal principal) {
    return auth.changeRole(id, request.role(), principal.getName());
  }
}
