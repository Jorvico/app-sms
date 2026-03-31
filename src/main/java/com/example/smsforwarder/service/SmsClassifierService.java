package com.example.smsforwarder.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SmsClassifierService {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(?i)(?:\\$|cop\\s*|valor\\s*[: ]|monto\\s*[: ]|por\\s*)([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{2})?|[0-9]+(?:[.,][0-9]{2})?)"
    );

    public SmsAnalysisResult analyze(String sender, String message) {
        String text = (sender + " " + message).toLowerCase(Locale.ROOT);
        String sourceType = detectSourceType(text);
        String category = detectCategory(text);
        BigDecimal amount = extractAmount(message);
        String currency = text.contains("usd") ? "USD" : "COP";
        String paymentMethod = detectPaymentMethod(text, sourceType);

        return new SmsAnalysisResult(sourceType, category, amount, currency, paymentMethod);
    }

    private String detectSourceType(String text) {
        if (text.contains("nequi")) return "NEQUI";
        if (text.contains("daviplata")) return "DAVIPLATA";
        if (text.contains("bancolombia")) return "BANCOLOMBIA";
        if (text.contains("davivienda")) return "DAVIVIENDA";
        if (text.contains("bbva")) return "BBVA";
        if (text.contains("movii")) return "MOVII";
        if (text.contains("tarjeta")) return "TARJETA";
        return "OTRO";
    }

    private String detectCategory(String text) {
        if (containsAny(text, "pago recibido", "recibiste", "ingreso", "abono recibido", "te enviaron", "transferencia recibida")) {
            return "INGRESO";
        }
        if (containsAny(text, "compra", "pago realizado", "transferencia enviada", "débito", "debito", "retiro", "pse", "pagaste", "descuento")) {
            return "GASTO";
        }
        return "POR_REVISAR";
    }

    private String detectPaymentMethod(String text, String sourceType) {
        if (text.contains("nequi")) return "NEQUI";
        if (text.contains("daviplata")) return "DAVIPLATA";
        if (text.contains("tarjeta credito") || text.contains("tarjeta de credito")) return "TARJETA_CREDITO";
        if (text.contains("tarjeta debito") || text.contains("tarjeta débito")) return "TARJETA_DEBITO";
        if (text.contains("pse")) return "PSE";
        return sourceType;
    }

    private BigDecimal extractAmount(String message) {
        Matcher matcher = AMOUNT_PATTERN.matcher(message);
        if (!matcher.find()) {
            return null;
        }

        String raw = matcher.group(1).trim();
        String normalized = normalizeNumber(raw);
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalizeNumber(String raw) {
        String value = raw.replace(" ", "");

        if (value.contains(",") && value.contains(".")) {
            int lastComma = value.lastIndexOf(',');
            int lastDot = value.lastIndexOf('.');
            if (lastComma > lastDot) {
                value = value.replace(".", "").replace(",", ".");
            } else {
                value = value.replace(",", "");
            }
            return value;
        }

        if (value.contains(",")) {
            int digitsAfterComma = value.length() - value.lastIndexOf(',') - 1;
            if (digitsAfterComma == 2) {
                return value.replace(".", "").replace(",", ".");
            }
            return value.replace(",", "");
        }

        if (value.contains(".")) {
            int digitsAfterDot = value.length() - value.lastIndexOf('.') - 1;
            if (digitsAfterDot == 2) {
                return value.replace(",", "");
            }
            return value.replace(".", "");
        }

        return value;
    }

    private boolean containsAny(String text, String... values) {
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }
        return false;
    }
}
