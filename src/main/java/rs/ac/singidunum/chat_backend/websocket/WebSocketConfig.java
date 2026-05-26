package rs.ac.singidunum.chat_backend.websocket;

import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import rs.ac.singidunum.chat_backend.entities.StompPrincipal;

// Message broker, RabbitMQ, Apache Kafka

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue"); // STOMP SUBSCRIBE
        registry.setApplicationDestinationPrefixes("/app"); // STOMP ACTIONS (SEND, MESSAGE, ...)
        registry.setUserDestinationPrefix("/user"); // Spring specificna konfiguracija

        // SUBSCRIBE CHANNEL -> Ukoliko je potrebno odgovoriti samo jednom korisniku,
        // mogu koristiti user destination prefix

        // /topic
        // /user/topic
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp").setAllowedOrigins("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null) {
                    return null;
                }

                String username = accessor.getFirstNativeHeader("X-Username"); // Jwt token

                if (username == null) {
                    return null;
                }

                accessor.setUser(new StompPrincipal(username));

                return message;
            }
        });
    }
}
