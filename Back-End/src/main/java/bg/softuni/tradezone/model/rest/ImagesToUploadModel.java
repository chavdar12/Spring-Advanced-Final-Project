package bg.softuni.tradezone.model.rest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImagesToUploadModel extends DeleteAdvRequest {

    private String[] images;
}
