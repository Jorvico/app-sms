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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
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
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(requestFactory);
        this.objectMapper = new ObjectMapper();
    }

    @Async
    public void forwardSmsAsync(SmsMessage smsMessage) {
        try {
            forwardSms(smsMessage);
        } catch (Exception e) {
            System.out.println("No se pudo enviar correo para SMS ID " + smsMessage.getId() + ": " + e.getMessage());
        }
    }

    private void forwardSms(SmsMessage smsMessage) throws Exception {
        if (apiKey == null || apiKey.isBlank() || from == null || from.isBlank() || to == null || to.isBlank()) {
            System.out.println("Correo omitido: faltan RESEND_API_KEY, APP_MAIL_FROM o APP_MAIL_TO");
            return;
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
            System.out.println("Resend respondió error para SMS ID " + smsMessage.getId() + ": " + response.getBody());
        }
    }

    private String buildSubject(SmsMessage smsMessage) {
        String amount = formatAmount(smsMessage.getPaymentAmount());
        String branchFullName = resolveBranchFullName(smsMessage);

        if (amount != null && branchFullName != null) {
            return "Pago por $" + amount + " en " + branchFullName;
        }
        if (amount != null) {
            return "Pago por $" + amount;
        }
        if (branchFullName != null) {
            return "Pago en " + branchFullName;
        }
        return "Nuevo SMS recibido";
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
