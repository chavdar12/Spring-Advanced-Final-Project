package bg.softuni.tradezone.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import java.util.List;

@Getter
@Setter
@Entity(name = "channels")
@AllArgsConstructor
@NoArgsConstructor
public class Channel {

    @Id
    private String id;

    @OneToMany(mappedBy = "channel")
    private List<ChatMessage> messages;

    @ManyToMany(mappedBy = "subscribedTo")
    private List<UserProfile> subscribed;

    public Channel(String id) {
        this.id = id;
    }
}
