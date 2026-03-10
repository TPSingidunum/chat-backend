package rs.ac.singidunum.chat_backend.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

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
}
