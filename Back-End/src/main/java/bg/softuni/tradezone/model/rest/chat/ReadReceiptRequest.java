package bg.softuni.tradezone.model.rest.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReadReceiptRequest {

    private String channelId;
    private String username;
}
