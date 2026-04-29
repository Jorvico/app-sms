package com.example.smsforwarder.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_messages")
public class SmsMessage {

    public static final String EMAIL_PENDING = "PENDING";
    public static final String EMAIL_SENDING = "SENDING";
    public static final String EMAIL_SENT = "SENT";
    public static final String EMAIL_FAILED = "FAILED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String sender;

    @Column(nullable = false, length = 50)
    private String phoneNumber;

    @Column(nullable = false, length = 5000)
    private String message;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(precision = 14, scale = 2)
    private BigDecimal paymentAmount;

    @Column(length = 100)
    private String extractedBranchName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(nullable = false, length = 20)
    private String emailStatus = EMAIL_PENDING;

    @Column(nullable = false)
    private Integer emailRetryCount = 0;

    @Column(length = 2000)
    private String emailLastError;

    private LocalDateTime emailSentAt;

    private LocalDateTime nextEmailRetryAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (emailStatus == null || emailStatus.isBlank()) {
            emailStatus = EMAIL_PENDING;
        }
        if (emailRetryCount == null) {
            emailRetryCount = 0;
        }
        if (nextEmailRetryAt == null) {
            nextEmailRetryAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

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

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getExtractedBranchName() {
        return extractedBranchName;
    }

    public void setExtractedBranchName(String extractedBranchName) {
        this.extractedBranchName = extractedBranchName;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public String getEmailStatus() {
        return emailStatus;
    }

    public void setEmailStatus(String emailStatus) {
        this.emailStatus = emailStatus;
    }

    public Integer getEmailRetryCount() {
        return emailRetryCount;
    }

    public void setEmailRetryCount(Integer emailRetryCount) {
        this.emailRetryCount = emailRetryCount;
    }

    public String getEmailLastError() {
        return emailLastError;
    }

    public void setEmailLastError(String emailLastError) {
        this.emailLastError = emailLastError;
    }

    public LocalDateTime getEmailSentAt() {
        return emailSentAt;
    }

    public void setEmailSentAt(LocalDateTime emailSentAt) {
        this.emailSentAt = emailSentAt;
    }

    public LocalDateTime getNextEmailRetryAt() {
        return nextEmailRetryAt;
    }

    public void setNextEmailRetryAt(LocalDateTime nextEmailRetryAt) {
        this.nextEmailRetryAt = nextEmailRetryAt;
    }
}
