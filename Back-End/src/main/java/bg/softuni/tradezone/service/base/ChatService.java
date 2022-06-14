package bg.softuni.tradezone.service.base;

import bg.softuni.tradezone.model.rest.chat.ChatRestModel;

public interface ChatService {

    void saveAndSend(ChatRestModel message);
}
