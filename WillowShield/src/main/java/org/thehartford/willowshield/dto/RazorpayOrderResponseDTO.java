package org.thehartford.willowshield.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RazorpayOrderResponseDTO {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String keyId;
    private Integer policyId;
}
