package bg.softuni.tradezone.service.base;

import bg.softuni.tradezone.model.rest.Subscription;
import bg.softuni.tradezone.model.service.ChannelServiceModel;

public interface ChannelService {

    ChannelServiceModel create(String id);

    ChannelServiceModel subscribeToChannel(Subscription subscription);

    void unsubscribeFromChannel(Subscription subscription);
}
