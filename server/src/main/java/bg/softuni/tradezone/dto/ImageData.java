package bg.softuni.tradezone.dto;

import static java.lang.annotation.ElementType.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** A base64 PNG/JPEG data URL, the format the client sends images in. */
@Pattern(
    regexp = "data:image/(png|jpe?g);base64,[A-Za-z0-9+/=]+",
    message = "must be a PNG or JPEG data URL")
@Constraint(validatedBy = {})
@Target({FIELD, PARAMETER, RECORD_COMPONENT, TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ImageData {

  String message() default "must be a PNG or JPEG data URL";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
