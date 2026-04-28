package com.example.smsforwarder.service;

import com.example.smsforwarder.entity.Branch;

import java.math.BigDecimal;

public class ParsedPaymentInfo {
    private final BigDecimal amount;
    private final Branch branch;
    private final String extractedBranchName;

    public ParsedPaymentInfo(BigDecimal amount, Branch branch, String extractedBranchName) {
        this.amount = amount;
        this.branch = branch;
        this.extractedBranchName = extractedBranchName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Branch getBranch() {
        return branch;
    }

    public String getExtractedBranchName() {
        return extractedBranchName;
    }
}
