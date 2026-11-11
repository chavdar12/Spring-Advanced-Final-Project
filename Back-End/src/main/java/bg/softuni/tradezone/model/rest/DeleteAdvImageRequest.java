package bg.softuni.tradezone.model.rest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteAdvImageRequest extends DeleteAdvRequest {

    private Long photoId;
}
