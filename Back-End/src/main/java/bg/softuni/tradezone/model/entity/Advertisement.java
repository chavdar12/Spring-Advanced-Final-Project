package bg.softuni.tradezone.model.entity;

import bg.softuni.tradezone.model.enums.Condition;
import bg.softuni.tradezone.model.enums.DeliveryType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "advertisements")
@EqualsAndHashCode(callSuper = true)
public class Advertisement extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", referencedColumnName = "id")
    private UserProfile creator;

    @Column
    private String title;

    @Column
    private String description;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "advertisement_photo",
            joinColumns = @JoinColumn(name = "advertisement_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "photo_id", referencedColumnName = "id")
    )
    private List<Photo> photos;

    @ManyToMany(mappedBy = "favorites", fetch = FetchType.EAGER)
    private List<UserProfile> profilesWhichLikedIt;

    @ManyToMany(mappedBy = "viewed", fetch = FetchType.LAZY)
    private List<UserProfile> profilesWhichViewedIt;

    @Column(columnDefinition = "decimal")
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private Condition condition;

    @Column(name = "delivery_type")
    @Enumerated(EnumType.STRING)
    private DeliveryType delivery;

    @Column(name = "views")
    private Long views;

    @Column
    private LocalDateTime createdOn;

    public Advertisement() {
        this.setProfilesWhichLikedIt(new ArrayList<>());
        this.setProfilesWhichLikedIt(new ArrayList<>());
        this.setProfilesWhichViewedIt(new ArrayList<>());
        this.setPhotos(new ArrayList<>());
    }
}
