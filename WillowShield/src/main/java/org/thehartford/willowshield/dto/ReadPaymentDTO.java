package org.thehartford.willowshield.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReadPaymentDTO {
    private Integer paymentId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String status;
    private String transactionReference;
    private String policyNumber;
    private String policyType;
    private String vehicleType;
    private String customerName;
    private String customerEmail;
}
