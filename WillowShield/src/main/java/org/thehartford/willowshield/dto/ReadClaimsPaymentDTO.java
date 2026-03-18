package org.thehartford.willowshield.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReadClaimsPaymentDTO {
    private Integer paymentId;
    private Integer claimId;
    private String claimNumber;
    private String policyNumber;
    private String policyType;
    private String vehicleType;
    private BigDecimal amountPaid;
    private LocalDateTime paymentDate;
    private String paymentStatus;
    private String transactionReference;
    private String adminName;
    private String customerName;
    private String customerEmail;
    private String claimsOfficerName;
    private String claimsOfficerEmail;
    private String bankAccountNumber;
    private String ifscCode;
    private String accountHolderName;
}
