package com.birp.chat_backend.dto.websocket;

import lombok.Data;

@Data
public class DirectMessage {
    private String type = "DIRECT_MESSAGE";
    private int toUserId;
    private String content;
}