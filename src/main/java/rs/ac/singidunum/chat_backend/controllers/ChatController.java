package rs.ac.singidunum.chat_backend.controllers;

import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import rs.ac.singidunum.chat_backend.dtos.chat.LoginRequest;

import java.util.HashMap;

@Controller
@AllArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/auth.login")
    @SendToUser("/queue/auth")
    public HashMap<String, String> login(LoginRequest loginRequest) {
        // Logica funkcije

        System.out.println(loginRequest.toString());

        HashMap <String, String> map = new HashMap<>();
        map.put("result", "success");
        map.put("token", "token");

        return map;
    }
}
