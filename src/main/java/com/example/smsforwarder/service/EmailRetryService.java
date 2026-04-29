package com.example.smsforwarder.service;

import com.example.smsforwarder.entity.SmsMessage;
import com.example.smsforwarder.repository.SmsMessageRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmailRetryService {

    private final SmsMessageRepository smsMessageRepository;
    private final MailService mailService;

    public EmailRetryService(SmsMessageRepository smsMessageRepository, MailService mailService) {
        this.smsMessageRepository = smsMessageRepository;
        this.mailService = mailService;
    }

    @Scheduled(fixedDelayString = "${app.email.retry.fixed-delay-ms:5000}")
    @Transactional
    public void retryPendingEmails() {
        LocalDateTime now = LocalDateTime.now();

        List<SmsMessage> messages = smsMessageRepository
                .findTop20ByEmailStatusInAndNextEmailRetryAtBeforeOrderByCreatedAtAsc(
                        List.of(SmsMessage.EMAIL_PENDING, SmsMessage.EMAIL_FAILED, SmsMessage.EMAIL_SENDING),
                        now
                );

        for (SmsMessage smsMessage : messages) {
            if (SmsMessage.EMAIL_SENT.equals(smsMessage.getEmailStatus())) {
                continue;
            }

            sendOne(smsMessage);
        }
    }

    private void sendOne(SmsMessage smsMessage) {
        // Estado intermedio para evitar que otro ciclo tome el mismo registro mientras se envía.
        smsMessage.setEmailStatus(SmsMessage.EMAIL_SENDING);
        smsMessage.setNextEmailRetryAt(LocalDateTime.now().plusMinutes(2));
        smsMessageRepository.saveAndFlush(smsMessage);

        try {
            mailService.sendSmsNotification(smsMessage);

            smsMessage.setEmailStatus(SmsMessage.EMAIL_SENT);
            smsMessage.setEmailSentAt(LocalDateTime.now());
            smsMessage.setEmailLastError(null);
            smsMessage.setNextEmailRetryAt(null);
            smsMessageRepository.save(smsMessage);

            System.out.println("Correo enviado OK para SMS ID " + smsMessage.getId());
        } catch (Exception e) {
            int retryCount = smsMessage.getEmailRetryCount() == null ? 0 : smsMessage.getEmailRetryCount();
            retryCount++;

            smsMessage.setEmailRetryCount(retryCount);
            smsMessage.setEmailStatus(SmsMessage.EMAIL_FAILED);
            smsMessage.setEmailLastError(limitError(e.getMessage()));
            smsMessage.setNextEmailRetryAt(LocalDateTime.now().plusSeconds(30));
            smsMessageRepository.save(smsMessage);

            System.out.println("Error enviando correo para SMS ID " + smsMessage.getId()
                    + " intento " + retryCount + ": " + e.getMessage());
        }
    }

    private String limitError(String value) {
        if (value == null) {
            return "Error desconocido";
        }
        return value.length() > 1900 ? value.substring(0, 1900) : value;
    }
}
