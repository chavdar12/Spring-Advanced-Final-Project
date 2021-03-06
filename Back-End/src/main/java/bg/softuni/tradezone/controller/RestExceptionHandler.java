package bg.softuni.tradezone.controller;

import bg.softuni.tradezone.error.ApiError;
import bg.softuni.tradezone.error.exception.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {


    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {

        String error = "Malformed JSON request";
        return buildResponseEntity(new ApiError(BAD_REQUEST, error, ex));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        return buildResponseEntity(ex);
    }

    @ExceptionHandler(NotAllowedException.class)
    protected ResponseEntity<Object> handleNotAllowed(NotAllowedException ex) {
        return buildResponseEntity(ex);
    }

    @ExceptionHandler(AdvertisementNotValidException.class)
    protected ResponseEntity<Object> handleAdvertisementNotValid(AdvertisementNotValidException ex) {
        return buildResponseEntity(ex);
    }

    @ExceptionHandler(CategoryNotValidException.class)
    protected ResponseEntity<Object> handleCategoryNotValid(CategoryNotValidException ex) {
        return buildResponseEntity(ex);
    }

    @ExceptionHandler(PhotoNotValidException.class)
    protected ResponseEntity<Object> handlePhotoNotValid(PhotoNotValidException ex) {
        return buildResponseEntity(ex);
    }

    @ExceptionHandler(SearchNotValidException.class)
    protected ResponseEntity<Object> handleSearchNotValid(SearchNotValidException ex) {
        return buildResponseEntity(ex);
    }

    @ExceptionHandler(DeleteRequestNotValidException.class)
    protected ResponseEntity<Object> handleDeleteRequestNotValid(DeleteRequestNotValidException ex) {
        return buildResponseEntity(ex);
    }

    @ExceptionHandler(ProfileNotCompletedException.class)
    protected ResponseEntity<Object> handleProfileNotCompleted(ProfileNotCompletedException ex) {
        return buildResponseEntity(ex);
    }

    @ExceptionHandler(ProfileUpdateNotValidException.class)
    protected ResponseEntity<Object> handleProfileUpdateNotValid(ProfileUpdateNotValidException ex) {
        return buildResponseEntity(ex);
    }

    @ExceptionHandler(PasswordUpdateNotValidException.class)
    protected ResponseEntity<Object> handlePasswordUpdateNotValid(PasswordUpdateNotValidException ex) {
        return buildResponseEntity(ex);
    }

    @ExceptionHandler(ViewsUpdateNotValidException.class)
    protected ResponseEntity<Object> handleViewsUpdateNotValid(ViewsUpdateNotValidException ex) {
        return buildResponseEntity(ex);
    }

    @ExceptionHandler(MessageToSendNotValidException.class)
    protected ResponseEntity<Object> handleMessageNotValid(MessageToSendNotValidException ex) {
        return buildResponseEntity(ex);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    private ResponseEntity<Object> buildResponseEntity(Throwable throwable) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage(throwable.getMessage());
        apiError.setDebugMessage(throwable.getLocalizedMessage());
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
