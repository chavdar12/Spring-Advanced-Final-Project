package bg.softuni.tradezone.controller;

import bg.softuni.tradezone.model.rest.chat.ChatRestModel;
import bg.softuni.tradezone.service.base.ChatService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Sends a message to its destination channel
     */
    @MessageMapping("/messages")
    public void handleMessage(ChatRestModel message) {
        chatService.saveAndSend(message);
    }
}
