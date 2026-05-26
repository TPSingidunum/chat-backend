package rs.ac.singidunum.chat_backend.controllers;

import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import rs.ac.singidunum.chat_backend.dtos.chat.SendMessageRequest;
import rs.ac.singidunum.chat_backend.dtos.chat.LoginRequest;
import rs.ac.singidunum.chat_backend.dtos.chat.LoginResponse;
import rs.ac.singidunum.chat_backend.dtos.chat.SendMessageResponse;
import rs.ac.singidunum.chat_backend.dtos.crypto.E2EEMessageEnvelope;
import rs.ac.singidunum.chat_backend.entities.Message;
import rs.ac.singidunum.chat_backend.entities.User;
import rs.ac.singidunum.chat_backend.repositories.MessageRepository;
import rs.ac.singidunum.chat_backend.repositories.UserRepository;
import rs.ac.singidunum.chat_backend.websocket.ConnectedUsers;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConnectedUsers connectedUsers;
    private final ObjectMapper objectMapper;

    @MessageMapping("/auth.login")
    @SendToUser("/queue/auth")
    public LoginResponse login(LoginRequest loginRequest, @Header("simpSessionId") String sessionId) {

        connectedUsers.register(sessionId, loginRequest.getUsername());
        broadcastConnectedUsers();
        broadcastUserOfflineMessages(loginRequest.getUsername());

        return new LoginResponse(true,"You have successfully connected");
    }

    private void broadcastUserOfflineMessages(String username) {
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            return;
        }


        List<Message> messages = messageRepository.findByReceiverIdOrderByCreatedAtAsc(user.get().getUserId());

        // Map message u Envelope Messages E2EEMessage
        for (Message message : messages) {
            E2EEMessageEnvelope envelope = objectMapper.readValue(objectMapper.writeValueAsString(message), E2EEMessageEnvelope.class);
            simpMessagingTemplate.convertAndSendToUser(username, "/queue/messages", envelope);
        }
        messageRepository.deleteByReceiverId(user.get().getUserId());
    }

    @MessageMapping("/users.connected")
    @SendToUser("/queue/connected-users")
    public List<String> getConnectedUsers() {
        return connectedUsers.getConnectedUsers();
    }

    @MessageMapping("/chat.send")
    public void receiveMessage(E2EEMessageEnvelope envelope) {
        Optional<User> user = userRepository.findByUserId(envelope.getReceiverId());

        if (user.isEmpty()) {
            return;
        }

        String username = user.get().getUsername();
        if (connectedUsers.isOnline(username)) {
            simpMessagingTemplate.convertAndSendToUser(username, "/queue/messages", envelope);
        }

        Message message = new Message();
        message.setSenderId(envelope.getSenderId());
        message.setReceiverId(envelope.getReceiverId());
        message.setEnvelope(envelope.getEnvelope());
        messageRepository.save(message);
    }

    private void broadcastMessageToUsers(String sendingUser, String message) {
        SendMessageResponse response = new SendMessageResponse(sendingUser, message);
        simpMessagingTemplate.convertAndSend("/topic/chat", response);
    }

    void broadcastConnectedUsers() {
        System.out.println("Sending user information");
        List<String> users = connectedUsers.getConnectedUsers();
        simpMessagingTemplate.convertAndSend("/topic/users", users);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        connectedUsers.remove(event.getSessionId());
        broadcastConnectedUsers();
    }
}
