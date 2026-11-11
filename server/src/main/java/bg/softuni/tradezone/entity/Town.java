package bg.softuni.tradezone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "towns")
public class Town extends BaseEntity {

  @NotBlank private String name;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Region region;

  @OneToMany(mappedBy = "town")
  private List<UserProfile> citizens = new ArrayList<>();
}
