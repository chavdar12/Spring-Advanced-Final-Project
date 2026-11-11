package bg.softuni.tradezone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "regions")
public class Region extends BaseEntity {

  @NotBlank private String name;

  @OneToMany(mappedBy = "region")
  private List<Town> towns = new ArrayList<>();
}
