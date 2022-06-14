package bg.softuni.tradezone.service.validation;

import bg.softuni.tradezone.model.rest.search.MessageToSend;
import org.springframework.stereotype.Service;

@Service
public class MessageToSendValidationService implements ValidationService<MessageToSend> {
    @Override
    public boolean isValid(MessageToSend element) {
        return true;
    }
}
