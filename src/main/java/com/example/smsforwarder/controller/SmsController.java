package com.example.smsforwarder.controller;

import com.example.smsforwarder.dto.SmsRequest;
import com.example.smsforwarder.dto.SmsResponse;
import com.example.smsforwarder.entity.SmsMessage;
import com.example.smsforwarder.service.SmsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    private final SmsService smsService;

    @Value("${app.api.key}")
    private String expectedApiKey;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping
    public ResponseEntity<?> receiveSms(
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey,
            @Valid @RequestBody SmsRequest request) {

        if (apiKey == null || !apiKey.equals(expectedApiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "message", "API key inválida"));
        }

        SmsMessage saved = smsService.saveAndQueueEmail(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "ok", true,
                "message", "SMS guardado. El correo queda en cola y se reintenta hasta enviarse",
                "data", toResponse(saved)
        ));
    }

    @GetMapping
    public ResponseEntity<?> listMessages(
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (apiKey == null || !apiKey.equals(expectedApiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "message", "API key inválida"));
        }

        int normalizedSize = Math.min(Math.max(size, 1), 100);
        Page<SmsMessage> result = smsService.listLatest(Math.max(page, 0), normalizedSize);
        List<SmsResponse> items = result.getContent().stream().map(this::toResponse).toList();

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "items", items
        ));
    }

    private SmsResponse toResponse(SmsMessage smsMessage) {
        String branchName = smsMessage.getBranch() != null ? smsMessage.getBranch().getName() : smsMessage.getExtractedBranchName();
        String branchFullName = smsMessage.getBranch() != null ? smsMessage.getBranch().getFullName() : null;

        return new SmsResponse(
                smsMessage.getId(),
                smsMessage.getSender(),
                smsMessage.getPhoneNumber(),
                smsMessage.getMessage(),
                smsMessage.getReceivedAt(),
                smsMessage.getCreatedAt(),
                smsMessage.getPaymentAmount(),
                branchName,
                branchFullName,
                smsMessage.getEmailStatus(),
                smsMessage.getEmailRetryCount(),
                smsMessage.getEmailLastError(),
                smsMessage.getEmailSentAt(),
                smsMessage.getNextEmailRetryAt()
        );
    }
}
