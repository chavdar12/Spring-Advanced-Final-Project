package bg.softuni.tradezone.controller;

import bg.softuni.tradezone.model.entity.ChatMessage;
import bg.softuni.tradezone.model.rest.chat.ChatRestModel;
import bg.softuni.tradezone.model.rest.chat.ReadReceiptRequest;
import bg.softuni.tradezone.repository.ChatMessageRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/messages")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@AllArgsConstructor
public class MessageController {

    private final ChatMessageRepository messageRepository;
    private final ModelMapper mapper;

    @GetMapping("/{channelId}")
    public Page<ChatRestModel> findMessages(Pageable pageable, @PathVariable("channelId") String channelId) {

        Page<ChatMessage> messages = messageRepository.findAllByChannelIdOrderByTimestampDesc(channelId, pageable);

        return messages.map(x -> {
            ChatRestModel model = mapper.map(x, ChatRestModel.class);
            model.setSender(x.getSender().getUser().getUsername());
            model.setChannelId(x.getChannel().getId());
            model.setSenderAvatarUrl(x.getSender().getPhoto().getUrl());
            return model;
        });
    }

    @PostMapping("/read")
    public void sendReadReceipt(@RequestBody ReadReceiptRequest request) {
        messageRepository.sendReadReceipt(request.getChannelId(), request.getUsername());
    }
}
