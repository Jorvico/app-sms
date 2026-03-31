package com.example.smsforwarder.service;

import com.example.smsforwarder.entity.SmsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class MailService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.mail.api-key:}")
    private String apiKey;

    @Value("${app.mail.from:}")
    private String from;

    @Value("${app.mail.to:}")
    private String to;

    public MailService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public void forwardSms(SmsMessage smsMessage) {
        if (apiKey == null || apiKey.isBlank() || from == null || from.isBlank() || to == null || to.isBlank()) {
            throw new IllegalStateException("Faltan variables de correo: RESEND_API_KEY, APP_MAIL_FROM o APP_MAIL_TO");
        }

        String url = "https://api.resend.com/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("from", from);
        body.put("to", new String[]{to});
        body.put("subject", "SMS recibido de " + smsMessage.getSender());
        body.put("html", buildHtml(smsMessage));

        try {
            String json = objectMapper.writeValueAsString(body);
            HttpEntity<String> request = new HttpEntity<>(json, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error enviando correo con Resend: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar el correo usando API HTTPS", e);
        }
    }

    private String buildHtml(SmsMessage smsMessage) {
        return """
                <h2>Nuevo SMS recibido</h2>
                <p><strong>Remitente:</strong> %s</p>
                <p><strong>Número:</strong> %s</p>
                <p><strong>Fecha del SMS:</strong> %s</p>
                <p><strong>Fecha de registro:</strong> %s</p>
                <p><strong>Mensaje:</strong></p>
                <pre>%s</pre>
                """.formatted(
                escapeHtml(smsMessage.getSender()),
                escapeHtml(smsMessage.getPhoneNumber()),
                smsMessage.getReceivedAt(),
                smsMessage.getCreatedAt(),
                escapeHtml(smsMessage.getMessage())
        );
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
