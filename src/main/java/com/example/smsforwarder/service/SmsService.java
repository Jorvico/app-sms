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

    public SmsService(SmsMessageRepository smsMessageRepository, MailService mailService) {
        this.smsMessageRepository = smsMessageRepository;
        this.mailService = mailService;
    }

    @Transactional
    public SmsMessage saveAndForward(SmsRequest request) {
        SmsMessage smsMessage = new SmsMessage();
        smsMessage.setSender(request.getSender().trim());
        smsMessage.setPhoneNumber(request.getPhoneNumber().trim());
        smsMessage.setMessage(request.getMessage().trim());
        smsMessage.setReceivedAt(resolveReceivedAt(request.getReceivedAt()));

        SmsMessage saved = smsMessageRepository.save(smsMessage);
        mailService.forwardSms(saved);
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
}
