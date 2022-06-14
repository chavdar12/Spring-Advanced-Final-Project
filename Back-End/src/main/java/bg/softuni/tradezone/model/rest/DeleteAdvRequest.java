package bg.softuni.tradezone.model.rest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteAdvRequest {

    private Long advertisementId;

    private String username;
}
