package org.thehartford.willowshield.entity;

import jakarta.persistence.*;
import lombok.*;
import org.thehartford.willowshield.enums.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "policy_plans")
@Getter
@Setter
public class PolicyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer planId;

    private String planName;
    private String policyType;
    private String description;

    private BigDecimal basePremium;
    private BigDecimal maxCoverageAmount;

    private Integer policyDurationMonths;
    private BigDecimal deductibleAmount;


    private boolean coversThirdParty;
    private boolean coversOwnDamage;
    private boolean coversTheft;
    private boolean coversNaturalDisaster;

    private boolean zeroDepreciationAvailable;
    private boolean engineProtectionAvailable;
    private boolean roadsideAssistanceAvailable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType applicableVehicleType;

    private boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "plan")
    private List<Policy> policies;

}
