package bg.softuni.tradezone.error;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.databind.DatabindException;

/**
 * Every error leaves the API as an RFC 9457 {@code application/problem+json} body: {@code {type,
 * title, status, detail, instance}} plus {@code errors: {field: message}} for validation failures.
 * Spring's own MVC exceptions (malformed JSON, 404, 405, 413, {@link ApiException}...) are handled
 * by the base class.
 */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

  private static ProblemDetail invalid(Map<String, String> errors) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Some fields are invalid");
    problem.setTitle("Validation failed");
    problem.setProperty("errors", errors);
    return problem;
  }

  private static String message(FieldError error) {
    // type mismatches (e.g. "abc" for a number or an unknown enum value) carry a long technical
    // message
    return error.isBindingFailure() ? "has an invalid value" : error.getDefaultMessage();
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    Map<String, String> errors = new TreeMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(e -> errors.putIfAbsent(e.getField(), message(e)));
    ex.getBindingResult()
        .getGlobalErrors()
        .forEach(e -> errors.putIfAbsent(e.getObjectName(), e.getDefaultMessage()));
    return handleExceptionInternal(ex, invalid(errors), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleHandlerMethodValidationException(
      HandlerMethodValidationException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    Map<String, String> errors = new TreeMap<>();
    ex.getParameterValidationResults()
        .forEach(r ->
            errors.putIfAbsent(
                r.getMethodParameter().getParameterName(),
                r.getResolvableErrors().getFirst().getDefaultMessage()));
    return handleExceptionInternal(ex, invalid(errors), headers, status, request);
  }

  /** A wrongly typed JSON value (e.g. an unknown enum constant) is reported against its field. */
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    if (ex.getCause() instanceof DatabindException e
        && !e.getPath().isEmpty()
        && e.getPath().getLast().getPropertyName() != null) {
      Map<String, String> errors =
          Map.of(e.getPath().getLast().getPropertyName(), "has an invalid value");
      return handleExceptionInternal(ex, invalid(errors), headers, status, request);
    }
    return super.handleHttpMessageNotReadable(ex, headers, status, request);
  }

  @ExceptionHandler
  ProblemDetail constraintViolation(ConstraintViolationException ex) {
    Map<String, String> errors = new TreeMap<>();
    ex.getConstraintViolations()
        .forEach(v -> errors.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
    return invalid(errors);
  }

  @ExceptionHandler
  ProblemDetail authentication(AuthenticationException ex) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.UNAUTHORIZED, "Invalid username or password");
  }

  @ExceptionHandler
  ProblemDetail accessDenied(AccessDeniedException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "You are not allowed to do that");
  }

  @ExceptionHandler
  ProblemDetail dataIntegrity(DataIntegrityViolationException ex) {
    log.debug("Constraint violated", ex);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT, "The request conflicts with existing data");
  }

  @ExceptionHandler
  ProblemDetail unexpected(Exception ex) {
    log.error("Unhandled error", ex);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on our side");
  }
}
