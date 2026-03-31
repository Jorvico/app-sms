package com.example.smsforwarder.dto;

import java.time.LocalDateTime;

public class SmsResponse {
    private final Long id;
    private final String sender;
    private final String phoneNumber;
    private final String message;
    private final LocalDateTime receivedAt;
    private final LocalDateTime createdAt;

    public SmsResponse(Long id, String sender, String phoneNumber, String message, LocalDateTime receivedAt, LocalDateTime createdAt) {
        this.id = id;
        this.sender = sender;
        this.phoneNumber = phoneNumber;
        this.message = message;
        this.receivedAt = receivedAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
