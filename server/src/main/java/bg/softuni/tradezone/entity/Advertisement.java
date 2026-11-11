package bg.softuni.tradezone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "advertisements")
public class Advertisement extends BaseEntity {

  @NotBlank
  @Size(max = 20)
  private String title;

  @NotBlank
  @Size(max = 1000)
  private String description;

  @NotNull
  @DecimalMin("0")
  private BigDecimal price;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "state")
  private Condition condition;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "delivery_type")
  private DeliveryType delivery;

  @PositiveOrZero private long views;

  @NotNull private LocalDateTime createdOn = LocalDateTime.now();

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Category category;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private UserProfile creator;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinTable(name = "advertisement_photo", inverseJoinColumns = @JoinColumn(name = "photo_id"))
  @OrderBy("id")
  private List<Photo> photos = new ArrayList<>();

  @ManyToMany(mappedBy = "favorites")
  private Set<UserProfile> likedBy = new HashSet<>();

  public String getImageUrl() {
    return photos.isEmpty() ? null : photos.getFirst().getUrl();
  }
}
