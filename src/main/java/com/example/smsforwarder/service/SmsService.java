package com.example.smsforwarder.service;

import com.example.smsforwarder.dto.SmsRequest;
import com.example.smsforwarder.entity.SmsMessage;
import com.example.smsforwarder.repository.SmsMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Service
public class SmsService {

    private final SmsMessageRepository smsMessageRepository;
    private final MailService mailService;
    private final PaymentMessageParserService parserService;

    public SmsService(SmsMessageRepository smsMessageRepository,
                      MailService mailService,
                      PaymentMessageParserService parserService) {
        this.smsMessageRepository = smsMessageRepository;
        this.mailService = mailService;
        this.parserService = parserService;
    }

    @Transactional
    public SmsMessage saveAndForward(SmsRequest request) {
        String message = safeTrim(request.getMessage());
        ParsedPaymentInfo parsedInfo = parserService.parse(message);

        SmsMessage smsMessage = new SmsMessage();
        smsMessage.setSender(defaultIfBlank(request.getSender(), "DESCONOCIDO"));
        smsMessage.setPhoneNumber(defaultIfBlank(request.getPhoneNumber(), "N/A"));
        smsMessage.setMessage(message);
        smsMessage.setReceivedAt(resolveReceivedAt(request.getReceivedAt()));
        smsMessage.setPaymentAmount(parsedInfo.getAmount());
        smsMessage.setExtractedBranchName(parsedInfo.getExtractedBranchName());
        smsMessage.setBranch(parsedInfo.getBranch());

        SmsMessage saved = smsMessageRepository.save(smsMessage);

        // El correo nunca debe tumbar la captura del SMS.
        // Se intenta enviar en segundo plano y cualquier error queda en logs.
        mailService.forwardSmsAsync(saved);

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<SmsMessage> listLatest(int page, int size) {
        return smsMessageRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    private LocalDateTime resolveReceivedAt(String receivedAt) {
        if (receivedAt == null || receivedAt.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(receivedAt.trim());
        } catch (DateTimeParseException ex) {
            return LocalDateTime.now();
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
