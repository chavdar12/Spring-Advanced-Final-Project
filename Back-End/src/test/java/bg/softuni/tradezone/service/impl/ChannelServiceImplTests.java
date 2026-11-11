package bg.softuni.tradezone.service.impl;

import bg.softuni.tradezone.error.exception.EntityNotFoundException;
import bg.softuni.tradezone.error.exception.NotAllowedException;
import bg.softuni.tradezone.model.entity.Channel;
import bg.softuni.tradezone.model.entity.User;
import bg.softuni.tradezone.model.entity.UserProfile;
import bg.softuni.tradezone.model.rest.Subscription;
import bg.softuni.tradezone.model.service.ChannelServiceModel;
import bg.softuni.tradezone.repository.ChannelRepository;
import bg.softuni.tradezone.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelServiceImplTests {

    @Mock
    ChannelRepository channelRepository;

    @Mock
    UserProfileRepository userProfileRepository;

    ChannelServiceImpl channelService;

    UserProfile profile;

    Channel channel;

    Subscription subscription;

    @BeforeEach
    void init() {
        channelService = new ChannelServiceImpl(channelRepository, userProfileRepository, new ModelMapper());
        User user = new User("pesho", "pesho@mail.bg", "pass");
        profile = new UserProfile(user);
        channel = new Channel("pesho-gosho");
        subscription = new Subscription();
        subscription.setUsername("pesho");
        subscription.setChannelId(channel.getId());
    }

    @Test
    void create_savesNewChannel() {
        when(channelRepository.existsById("new")).thenReturn(false);
        when(channelRepository.save(any(Channel.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals("new", channelService.create("new").getId());
    }

    @Test
    void create_throws_whenChannelExists() {
        when(channelRepository.existsById("dup")).thenReturn(true);

        assertThrows(EntityNotFoundException.class, () -> channelService.create("dup"));
    }

    @Test
    void subscribe_addsChannelAndReturnsIt() {
        when(userProfileRepository.findByUserUsername("pesho")).thenReturn(Optional.of(profile));
        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        ChannelServiceModel result = channelService.subscribeToChannel(subscription);

        assertEquals(channel.getId(), result.getId());
        assertTrue(profile.getSubscribedTo().contains(channel));
        verify(userProfileRepository).save(profile);
    }

    @Test
    void subscribe_throws_whenAlreadySubscribed() {
        profile.getSubscribedTo().add(channel);
        when(userProfileRepository.findByUserUsername("pesho")).thenReturn(Optional.of(profile));
        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        assertThrows(NotAllowedException.class, () -> channelService.subscribeToChannel(subscription));
    }

    @Test
    void unsubscribe_removesChannel() {
        profile.getSubscribedTo().add(channel);
        when(userProfileRepository.findByUserUsername("pesho")).thenReturn(Optional.of(profile));
        when(channelRepository.findById(channel.getId())).thenReturn(Optional.of(channel));

        channelService.unsubscribeFromChannel(subscription);

        assertTrue(profile.getSubscribedTo().isEmpty());
        verify(userProfileRepository).save(profile);
    }
}
