package com.birp.chat_backend.dto.websocket;

import lombok.Data;

@Data
public class ChallengeMessage {
    private String type = "CHALLENGE";
    private String encryptedChallenge;
}