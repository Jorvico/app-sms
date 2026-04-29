package com.example.smsforwarder.service;

import com.example.smsforwarder.entity.Branch;
import com.example.smsforwarder.entity.SmsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class MailService {

    private static final DateTimeFormatter SUBJECT_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.mail.api-key:}")
    private String apiKey;

    @Value("${app.mail.from:}")
    private String from;

    @Value("${app.mail.to:}")
    private String to;

    public MailService() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(requestFactory);
        this.objectMapper = new ObjectMapper();
    }

    public void sendSmsNotification(SmsMessage smsMessage) throws Exception {
        if (apiKey == null || apiKey.isBlank() || from == null || from.isBlank() || to == null || to.isBlank()) {
            throw new IllegalStateException("Faltan RESEND_API_KEY, APP_MAIL_FROM o APP_MAIL_TO");
        }

        String url = "https://api.resend.com/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("from", from);
        body.put("to", new String[]{to});
        body.put("subject", buildSubject(smsMessage));
        body.put("html", buildHtml(smsMessage));

        String json = objectMapper.writeValueAsString(body);
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Resend respondió " + response.getStatusCode() + ": " + response.getBody());
        }
    }

    private String buildSubject(SmsMessage smsMessage) {
        String amount = formatAmount(smsMessage.getPaymentAmount());
        String branchFullName = resolveBranchFullName(smsMessage);
        String time = resolveSubjectTime(smsMessage.getReceivedAt());

        if (amount != null && branchFullName != null) {
            return "Pago $" + amount + " en " + branchFullName + " [" + time + "]";
        }
        if (amount != null) {
            return "Pago $" + amount + " [" + time + "]";
        }
        if (branchFullName != null) {
            return "Pago en " + branchFullName + " [" + time + "]";
        }
        return "Nuevo SMS recibido [" + time + "]";
    }

    private String resolveSubjectTime(LocalDateTime receivedAt) {
        if (receivedAt != null) {
            return receivedAt.format(SUBJECT_TIME_FORMAT);
        }
        return LocalTime.now().format(SUBJECT_TIME_FORMAT);
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
        return formatter.format(amount);
    }

    private String resolveBranchFullName(SmsMessage smsMessage) {
        Branch branch = smsMessage.getBranch();
        if (branch != null && branch.getFullName() != null && !branch.getFullName().isBlank()) {
            return branch.getFullName();
        }
        return null;
    }

    private String buildHtml(SmsMessage smsMessage) {
        return """
                <p><strong>Valor:</strong> %s</p>
                <p><strong>Sucursal:</strong> %s</p>
                <hr>
                <p><strong>Fecha del SMS:</strong> %s</p>
                <p><strong>Mensaje:</strong></p>
                <pre>%s</pre>
                """.formatted(
                escapeHtml(formatAmount(smsMessage.getPaymentAmount())),
                escapeHtml(resolveBranchFullName(smsMessage)),
                smsMessage.getReceivedAt(),
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
