package org.thehartford.willowshield.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.thehartford.willowshield.enums.VehicleType;

import java.math.BigDecimal;
@Data
public class CreatePolicyPlanDTO {

    @NotBlank
    private String planName;

    @NotBlank
    private String policyType;

    @NotBlank
    private String description;

    @NotNull @Positive
    private BigDecimal basePremium;

    @NotNull @Positive
    private BigDecimal maxCoverageAmount;

    @NotNull @Positive
    private Integer policyDurationMonths;

    private boolean isActive = true;

    private BigDecimal deductibleAmount;
    private BigDecimal agentCommissionPercentage;

    private boolean coversThirdParty;
    private boolean coversOwnDamage;
    private boolean coversTheft;
    private boolean coversNaturalDisaster;

    private boolean zeroDepreciationAvailable;
    private boolean engineProtectionAvailable;
    private boolean roadsideAssistanceAvailable;

    @Enumerated(EnumType.STRING)
    @NotNull
    private VehicleType applicableVehicleType;

}