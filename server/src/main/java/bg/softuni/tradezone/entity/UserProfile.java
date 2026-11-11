package bg.softuni.tradezone.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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
@Table(name = "profiles")
public class UserProfile extends BaseEntity {

  @OneToOne(mappedBy = "profile", fetch = FetchType.LAZY)
  private User user;

  @Size(max = 12)
  private String firstName;

  @Size(max = 12)
  private String lastName;

  @Size(max = 100)
  private String aboutMe;

  @ManyToOne(fetch = FetchType.LAZY)
  private Town town;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private Photo photo;

  private boolean connected;

  /** Name, town and picture are set: the user may publish and chat. */
  private boolean isCompleted;

  @OneToMany(mappedBy = "creator")
  private List<Advertisement> createdAdvertisements = new ArrayList<>();

  @ManyToMany
  @JoinTable(
      name = "profiles_favorites",
      joinColumns = @JoinColumn(name = "profile_id"),
      inverseJoinColumns = @JoinColumn(name = "fav_advertisement_id"))
  private Set<Advertisement> favorites = new HashSet<>();

  @ManyToMany
  @JoinTable(
      name = "profiles_viewed",
      joinColumns = @JoinColumn(name = "profile_id"),
      inverseJoinColumns = @JoinColumn(name = "viewed_advertisement_id"))
  private Set<Advertisement> viewed = new HashSet<>();

  @ManyToMany
  @JoinTable(
      name = "profiles_channels",
      joinColumns = @JoinColumn(name = "profile_id"),
      inverseJoinColumns = @JoinColumn(name = "channel_id"))
  private Set<Channel> subscribedTo = new HashSet<>();

  UserProfile(User user) {
    this.user = user;
  }

  public String getUsername() {
    return user.getUsername();
  }

  public String getPhotoUrl() {
    return photo == null ? null : photo.getUrl();
  }

  public void completeIfReady() {
    isCompleted = firstName != null && lastName != null && town != null && photo != null;
  }
}
