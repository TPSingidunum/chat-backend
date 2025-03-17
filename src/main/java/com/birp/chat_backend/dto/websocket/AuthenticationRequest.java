package com.birp.chat_backend.dto.websocket;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String type = "AUTH_REQUEST";
    private String email;
}