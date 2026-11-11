package bg.softuni.tradezone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {

  @NotBlank
  @Size(max = 12)
  private String username;

  @Email @NotBlank private String email;

  @NotBlank private String password;

  @NotNull
  @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
  private UserProfile profile;

  // eager: the JWT filter needs authorities outside any transaction
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "users_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  // links both sides of the one-to-one; User can't be final because Hibernate proxies it
  @SuppressWarnings("this-escape")
  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.profile = new UserProfile(this);
  }

  @Override
  public Set<Role> getAuthorities() {
    return roles;
  }

  public RoleName topRole() {
    return roles.stream().map(Role::getName).max(Enum::compareTo).orElse(RoleName.ROLE_USER);
  }
}
