package bg.softuni.tradezone.web;

import bg.softuni.tradezone.dto.AuthResponse;
import bg.softuni.tradezone.dto.LoginRequest;
import bg.softuni.tradezone.dto.SignUpRequest;
import bg.softuni.tradezone.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService auth;

  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public void signUp(@Valid @RequestBody SignUpRequest request) {
    auth.signUp(request);
  }

  @PostMapping("/signin")
  public AuthResponse signIn(@Valid @RequestBody LoginRequest request) {
    return auth.signIn(request);
  }

  /** nginx auth_request target: 204 + X-User for a valid token, 401 otherwise. */
  @GetMapping("/verify")
  public ResponseEntity<Void> verify(Principal principal) {
    return ResponseEntity.noContent().header("X-User", principal.getName()).build();
  }
}
