package bg.softuni.tradezone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "photos")
public class Photo extends BaseEntity {

  @NotBlank private String idInCloud;

  @NotBlank private String url;

  public Photo(String idInCloud, String url) {
    this.idInCloud = idInCloud;
    this.url = url;
  }
}
