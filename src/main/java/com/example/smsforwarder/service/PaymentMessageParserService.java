package com.example.smsforwarder.service;

import com.example.smsforwarder.entity.Branch;
import com.example.smsforwarder.repository.BranchRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PaymentMessageParserService {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(?i)\\bpor\\s+([0-9]+(?:[.,][0-9]+)?)\\s+con\\s+comision");
    private static final Pattern BRANCH_PATTERN = Pattern.compile("(?i),\\s*a\\s+(.+?)\\s+en\\s+la\\s+terminal");

    private final BranchRepository branchRepository;

    public PaymentMessageParserService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public ParsedPaymentInfo parse(String message) {
        BigDecimal amount = extractAmount(message);
        String normalizedBranchName = normalizeBranchName(extractRawBranch(message));
        Branch branch = null;

        if (normalizedBranchName != null && !normalizedBranchName.isBlank()) {
            Optional<Branch> branchFound = branchRepository.findByNameIgnoreCase(normalizedBranchName);
            branch = branchFound.orElse(null);
        }

        return new ParsedPaymentInfo(amount, branch, normalizedBranchName);
    }

    private BigDecimal extractAmount(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }

        Matcher matcher = AMOUNT_PATTERN.matcher(message);
        if (!matcher.find()) {
            return null;
        }

        String rawAmount = matcher.group(1).replace(".", "").replace(",", ".");
        try {
            return new BigDecimal(rawAmount);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String extractRawBranch(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }

        Matcher matcher = BRANCH_PATTERN.matcher(message);
        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1).trim();
    }

    private String normalizeBranchName(String rawBranch) {
        if (rawBranch == null || rawBranch.isBlank()) {
            return null;
        }

        String normalized = rawBranch
                .toUpperCase(Locale.ROOT)
                .replace("DROGUERIA", "")
                .replace("FARMACENTER", "")
                .replaceAll("\\s+", " ")
                .trim();

        return normalized.isBlank() ? null : normalized;
    }
}
