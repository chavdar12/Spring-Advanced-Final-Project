package bg.softuni.tradezone.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

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
