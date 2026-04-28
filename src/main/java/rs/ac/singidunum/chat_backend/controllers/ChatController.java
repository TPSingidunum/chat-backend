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
import rs.ac.singidunum.chat_backend.websocket.ConnectedUsers;

import java.util.List;

@Controller
@AllArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConnectedUsers connectedUsers;

    @MessageMapping("/auth.login")
    @SendToUser("/queue/auth")
    public LoginResponse login(LoginRequest loginRequest, @Header("simpSessionId") String sessionId) {

        connectedUsers.register(sessionId, loginRequest.getUsername());
        broadcastConnectedUsers();

        return new LoginResponse(true,"You have successfully connected");
    }

    @MessageMapping("/users.connected")
    @SendToUser("/queue/connected-users")
    public List<String> getConnectedUsers() {
        return connectedUsers.getConnectedUsers();
    }

    @MessageMapping("/chat.send")
    public void receiveMessage(SendMessageRequest message, @Header("simpSessionId") String sessionId) {
        String sendingUser = connectedUsers.getUsername(sessionId);
        broadcastMessageToUsers(sendingUser, message.getPayload());
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
