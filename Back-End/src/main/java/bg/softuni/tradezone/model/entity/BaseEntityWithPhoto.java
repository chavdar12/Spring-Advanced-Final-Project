package bg.softuni.tradezone.model.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntityWithPhoto extends BaseEntity {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "photo_id", referencedColumnName = "id")
    private Photo photo;

    protected BaseEntityWithPhoto() {

    }
}


