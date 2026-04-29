package com.example.smsforwarder.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SmsResponse {
    private final Long id;
    private final String sender;
    private final String phoneNumber;
    private final String message;
    private final LocalDateTime receivedAt;
    private final LocalDateTime createdAt;
    private final BigDecimal paymentAmount;
    private final String branchName;
    private final String branchFullName;
    private final String emailStatus;
    private final Integer emailRetryCount;
    private final String emailLastError;
    private final LocalDateTime emailSentAt;
    private final LocalDateTime nextEmailRetryAt;

    public SmsResponse(Long id, String sender, String phoneNumber, String message,
                       LocalDateTime receivedAt, LocalDateTime createdAt,
                       BigDecimal paymentAmount, String branchName, String branchFullName,
                       String emailStatus, Integer emailRetryCount, String emailLastError,
                       LocalDateTime emailSentAt, LocalDateTime nextEmailRetryAt) {
        this.id = id;
        this.sender = sender;
        this.phoneNumber = phoneNumber;
        this.message = message;
        this.receivedAt = receivedAt;
        this.createdAt = createdAt;
        this.paymentAmount = paymentAmount;
        this.branchName = branchName;
        this.branchFullName = branchFullName;
        this.emailStatus = emailStatus;
        this.emailRetryCount = emailRetryCount;
        this.emailLastError = emailLastError;
        this.emailSentAt = emailSentAt;
        this.nextEmailRetryAt = nextEmailRetryAt;
    }

    public Long getId() { return id; }
    public String getSender() { return sender; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getMessage() { return message; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public String getBranchName() { return branchName; }
    public String getBranchFullName() { return branchFullName; }
    public String getEmailStatus() { return emailStatus; }
    public Integer getEmailRetryCount() { return emailRetryCount; }
    public String getEmailLastError() { return emailLastError; }
    public LocalDateTime getEmailSentAt() { return emailSentAt; }
    public LocalDateTime getNextEmailRetryAt() { return nextEmailRetryAt; }
}
