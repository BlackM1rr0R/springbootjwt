package com.example.jwtauth.dtos;

public class MessageIdRequest {
    private String messageId;
    public MessageIdRequest(String messageId) {
        this.messageId = messageId;
    }
    public String getMessageId() {
        return messageId;
    }
}
