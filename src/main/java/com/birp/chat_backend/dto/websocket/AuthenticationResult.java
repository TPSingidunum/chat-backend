package com.birp.chat_backend.dto.websocket;

import lombok.Data;

@Data
public class AuthenticationResult {
    private String type = "AUTH_RESULT";
    private boolean success;
    private String message;
    private String token;  // Optional JWT for subsequent requests
}