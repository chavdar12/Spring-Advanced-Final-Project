package bg.softuni.tradezone.model.rest;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class LoginForm {

    @NotBlank
    @Size(min = 3, max = 11)
    private String username;

    @NotBlank
    @Size(min = 3, max = 16)
    private String password;
}
