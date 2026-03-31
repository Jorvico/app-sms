package com.example.smsforwarder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SmsRequest {

    @NotBlank(message = "sender is required")
    @Size(max = 255)
    private String sender;

    @NotBlank(message = "phoneNumber is required")
    @Size(max = 50)
    private String phoneNumber;

    @NotBlank(message = "message is required")
    @Size(max = 5000)
    private String message;

    @Size(max = 50)
    private String receivedAt;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }
}
