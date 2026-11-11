package bg.softuni.tradezone.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * The only exception services throw on purpose; {@link ApiExceptionHandler} renders it as a
 * problem.
 */
public class ApiException extends ResponseStatusException {

  private ApiException(HttpStatus status, String detail) {
    super(status, detail);
  }

  public static ApiException notFound(String what, Object id) {
    return new ApiException(HttpStatus.NOT_FOUND, "%s '%s' was not found".formatted(what, id));
  }

  public static ApiException badRequest(String detail) {
    return new ApiException(HttpStatus.BAD_REQUEST, detail);
  }

  public static ApiException forbidden(String detail) {
    return new ApiException(HttpStatus.FORBIDDEN, detail);
  }

  public static ApiException badGateway(String detail) {
    return new ApiException(HttpStatus.BAD_GATEWAY, detail);
  }

  public static ApiException conflict(String detail) {
    return new ApiException(HttpStatus.CONFLICT, detail);
  }
}
