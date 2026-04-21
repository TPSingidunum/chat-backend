package rs.ac.singidunum.chat_backend.controllers;

import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import rs.ac.singidunum.chat_backend.dtos.chat.LoginRequest;
import rs.ac.singidunum.chat_backend.dtos.chat.LoginResponse;
import rs.ac.singidunum.chat_backend.websocket.ConnectedUsers;

@Controller
@AllArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConnectedUsers connectedUsers;

    @MessageMapping("/auth.login")
    @SendToUser("/queue/auth")
    public LoginResponse login(LoginRequest loginRequest, @Header("simpSessionId") String sessionId) {

        connectedUsers.register(sessionId,loginRequest.getUsername());
        broadcastConnectedUsers();

        return new LoginResponse(true,"You have successfully connected");
    }

    void broadcastConnectedUsers() {
        System.out.println("Sending user information");
        simpMessagingTemplate.convertAndSend(
        "/queue/auth",
                connectedUsers.getConnectedUsers()
        );
    }
}
