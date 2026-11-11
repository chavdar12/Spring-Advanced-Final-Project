package bg.softuni.tradezone.dto;

import bg.softuni.tradezone.entity.Condition;
import bg.softuni.tradezone.entity.DeliveryType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/** Create and edit body; {@code images} is only read (and required) on create. */
public record AdvertisementRequest(
    @NotBlank
        @Pattern(
            regexp = "[A-ZА-Я][A-ZА-Яа-яa-z\\s\\d]{4,19}",
            message = "must be 5-20 letters or digits, starting with a capital letter")
        String title,
    @NotBlank @Size(max = 1000) String description,
    @NotNull @DecimalMin("0.1") @DecimalMax("10000") BigDecimal price,
    @NotNull Condition condition,
    @NotNull DeliveryType delivery,
    @NotNull Long categoryId,
    @NotEmpty(groups = OnCreate.class) @Size(max = 10) List<@ImageData String> images) {

  public interface OnCreate {}
}
