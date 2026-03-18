package org.thehartford.willowshield.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequestDTO {
    private Integer policyId;
    private BigDecimal amount;
    private String transactionReference;
}
