package bg.softuni.tradezone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ProfileRequest(
    @NotBlank @Pattern(regexp = NAME, message = NAME_MESSAGE) String firstName,
    @NotBlank @Pattern(regexp = NAME, message = NAME_MESSAGE) String lastName,
    @NotNull Long townId,
    @NotBlank
        @Pattern(
            regexp = "[A-ZА-Я][a-zа-я ]{1,99}",
            message = "must start with a capital letter, up to 100 letters")
        String aboutMe) {

  private static final String NAME = "[A-ZА-Я][a-zа-я]{3,11}";
  private static final String NAME_MESSAGE = "must be a capitalized name of 4-12 letters";
}
