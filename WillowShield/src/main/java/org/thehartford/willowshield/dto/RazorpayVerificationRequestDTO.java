package org.thehartford.willowshield.dto;

import lombok.Data;

@Data
public class RazorpayVerificationRequestDTO {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private Integer policyId;
}
