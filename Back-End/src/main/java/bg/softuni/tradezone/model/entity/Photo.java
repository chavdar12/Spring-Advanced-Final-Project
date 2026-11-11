package bg.softuni.tradezone.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name = "photos")
@EqualsAndHashCode(callSuper = true)
public class Photo extends BaseEntity {

    @Column
    private String idInCloud;

    @Column
    private String url;
}
