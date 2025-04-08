package com.birp.chat_backend.dto.websocket;

import lombok.Data;

@Data
public class ChallengeResponse {
    private String type = "CHALLENGE_RESPONSE";
    private String encryptedResponse;
}