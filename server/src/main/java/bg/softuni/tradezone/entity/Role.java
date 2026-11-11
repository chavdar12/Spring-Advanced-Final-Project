package bg.softuni.tradezone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends BaseEntity implements GrantedAuthority {

  @NotNull
  @Enumerated(EnumType.STRING)
  private RoleName name;

  @Override
  public String getAuthority() {
    return name.name();
  }
}
