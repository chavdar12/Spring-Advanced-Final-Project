package bg.softuni.tradezone.controller;

import bg.softuni.tradezone.model.rest.Subscription;
import bg.softuni.tradezone.service.base.ChannelService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/channel")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping("/create")
    public ResponseEntity<?> createChannel(@RequestBody String channelId) {
        channelService.create(channelId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Subscription subscription) {
        channelService.subscribeToChannel(subscription);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody Subscription subscription) {
        channelService.unsubscribeFromChannel(subscription);
        return ResponseEntity.ok().build();
    }
}
