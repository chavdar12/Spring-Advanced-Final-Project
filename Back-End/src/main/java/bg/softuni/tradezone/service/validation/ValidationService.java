package bg.softuni.tradezone.service.validation;

public interface ValidationService<T> {

    boolean isValid(T element);
}
