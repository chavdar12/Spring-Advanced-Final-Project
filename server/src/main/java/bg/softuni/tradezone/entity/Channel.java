package bg.softuni.tradezone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** A one-to-one conversation; the id is both usernames, sorted and joined by a dot. */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "channels")
public class Channel {

  @Id private String id;

  public Channel(String id) {
    this.id = id;
  }
}
