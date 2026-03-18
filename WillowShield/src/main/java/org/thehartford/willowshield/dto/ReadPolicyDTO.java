package org.thehartford.willowshield.dto;

import lombok.Data;
import org.thehartford.willowshield.enums.PolicyStatus;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class ReadPolicyDTO {
    private Integer policyId;
    private String policyNumber;
    private PolicyStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal premiumAmount;
    private Long customerId;
    private String customerName;
    private Integer vehicleId;
    private String vehicleRegistrationNumber;
    private Integer planId;
    private String planName;
    private String policyType;
    private String description;
    private BigDecimal maxCoverageAmount;
    private BigDecimal deductibleAmount;
    private boolean coversThirdParty;
    private boolean coversOwnDamage;
    private boolean coversTheft;
    private boolean coversNaturalDisaster;
    private boolean expired;
}
