package bg.softuni.tradezone.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "regions")
@Getter
@Setter
@NoArgsConstructor
public class Region extends BaseEntity {

    @Column
    private String name;

    @OneToMany(mappedBy = "region")
    private List<Town> towns = new ArrayList<>();
}
