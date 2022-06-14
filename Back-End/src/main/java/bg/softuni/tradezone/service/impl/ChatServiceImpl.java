package bg.softuni.tradezone.service.impl;

import bg.softuni.tradezone.model.entity.Channel;
import bg.softuni.tradezone.model.entity.ChatMessage;
import bg.softuni.tradezone.model.entity.UserProfile;
import bg.softuni.tradezone.model.rest.chat.ChatRestModel;
import bg.softuni.tradezone.repository.ChannelRepository;
import bg.softuni.tradezone.repository.ChatMessageRepository;
import bg.softuni.tradezone.repository.UserProfileRepository;
import bg.softuni.tradezone.service.base.ChatService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@AllArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final SimpMessagingTemplate template;
    private final ChannelRepository channelRepository;
    private final ChatMessageRepository messageRepository;
    private final UserProfileRepository profileRepository;

    @Override
    public void saveAndSend(ChatRestModel message) {

        UserProfile sender = profileRepository
                .findByUserUsername(message.getSender())
                .orElseThrow();

        Channel channel = channelRepository.findById(message.getChannelId())
                .orElseThrow();

        message.setTimestamp(new Date());
        message.setSenderAvatarUrl(sender.getPhoto().getUrl());

        ChatMessage entity = new ChatMessage(sender, channel, message.getContent(), message.getTimestamp(), message.getReadDate());
        messageRepository.save(entity);


        template.convertAndSend("/channel/chat/" + message.getChannelId(), message);
    }
}
