package bg.softuni.tradezone.error.exception;

public class MessageToSendNotValidException extends RuntimeException {

    public MessageToSendNotValidException(String message) {
        super(message);
    }
}
