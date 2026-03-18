package org.thehartford.willowshield.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.thehartford.willowshield.enums.VehicleType;

import java.math.BigDecimal;
@Data
public class ReadPolicyPlanDTO {
    private Integer planId;
    private String planName;
    private String policyType;
    private String description;
    private BigDecimal basePremium;
    private BigDecimal maxCoverageAmount;
    private Integer policyDurationMonths;
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

    private BigDecimal deductibleAmount;
    private boolean isActive;
}
