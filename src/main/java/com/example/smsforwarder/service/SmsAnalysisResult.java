package com.example.smsforwarder.service;

import java.math.BigDecimal;

public record SmsAnalysisResult(
        String sourceType,
        String category,
        BigDecimal amount,
        String currency,
        String paymentMethod
) {
}
