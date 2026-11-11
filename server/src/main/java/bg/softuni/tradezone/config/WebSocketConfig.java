package bg.softuni.tradezone.config;

import bg.softuni.tradezone.security.JwtProvider;
import bg.softuni.tradezone.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private static final String CHAT_TOPIC = "/channel/chat/";

  private final JwtProvider jwt;
  private final UserDetailsService users;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/channel");
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // same-origin behind the gateway; ng serve proxies in development
    registry.addEndpoint("/wechat").withSockJS();
  }

  /**
   * CONNECT must carry a bearer token; a chat topic may only be subscribed to by its two
   * participants.
   */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(
        new ChannelInterceptor() {
          @Override
          public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor stomp =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (stomp == null) {
              return message;
            }
            if (StompCommand.CONNECT.equals(stomp.getCommand())) {
              String username = token(stomp);
              if (username == null) {
                throw new BadCredentialsException("Missing or invalid bearer token");
              }
              UserDetails user = users.loadUserByUsername(username);
              stomp.setUser(
                  UsernamePasswordAuthenticationToken.authenticated(
                      user, null, user.getAuthorities()));
            }
            String destination = stomp.getDestination();
            if (StompCommand.SUBSCRIBE.equals(stomp.getCommand())
                && destination != null
                && destination.startsWith(CHAT_TOPIC)
                && (stomp.getUser() == null
                    || !ChatService.isParticipant(
                        destination.substring(CHAT_TOPIC.length()), stomp.getUser().getName()))) {
              throw new AccessDeniedException("Not a participant of " + destination);
            }
            return message;
          }

          private String token(StompHeaderAccessor stomp) {
            String token =
                JwtProvider.bearer(stomp.getFirstNativeHeader(HttpHeaders.AUTHORIZATION));
            return token == null ? null : jwt.username(token);
          }
        });
  }
}
