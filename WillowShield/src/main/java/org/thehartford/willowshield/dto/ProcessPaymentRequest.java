package org.thehartford.willowshield.dto;

public class ProcessPaymentRequest {
    private String transactionReference;

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
}
