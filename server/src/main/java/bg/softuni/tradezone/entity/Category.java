package bg.softuni.tradezone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

  @NotBlank
  @Size(max = 50)
  private String name;

  @NotNull
  @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
  private Photo photo;

  @ManyToOne(fetch = FetchType.LAZY)
  private UserProfile creator;

  @OneToMany(mappedBy = "category")
  private List<Advertisement> advertisements = new ArrayList<>();

  public Category(String name, Photo photo, UserProfile creator) {
    this.name = name;
    this.photo = photo;
    this.creator = creator;
  }
}
