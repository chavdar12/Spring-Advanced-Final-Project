package bg.softuni.tradezone.model.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "categories")
public class Category extends BaseEntityWithPhoto {

    @ManyToOne
    @JoinColumn(name = "creator_id", referencedColumnName = "id")
    private UserProfile creator;

    @Column
    private String name;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Advertisement> advertisements;
}
