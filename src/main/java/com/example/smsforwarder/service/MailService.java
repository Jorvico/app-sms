package com.example.smsforwarder.service;

import com.example.smsforwarder.entity.SmsMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.to}")
    private String to;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void forwardSms(SmsMessage smsMessage) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(from);
        email.setTo(to);
        email.setSubject("SMS recibido de " + smsMessage.getSender());
        email.setText(buildBody(smsMessage));
        mailSender.send(email);
    }

    private String buildBody(SmsMessage smsMessage) {
        return "Se recibió un nuevo SMS\n\n"
                + "Remitente: " + smsMessage.getSender() + "\n"
                + "Número: " + smsMessage.getPhoneNumber() + "\n"
                + "Fecha del SMS: " + smsMessage.getReceivedAt() + "\n"
                + "Fecha de registro: " + smsMessage.getCreatedAt() + "\n\n"
                + "Mensaje:\n" + smsMessage.getMessage();
    }
}
