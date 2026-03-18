package org.thehartford.willowshield.entity;

import org.thehartford.willowshield.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "claims")
@Getter
@Setter
public class Claims {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer claimId;

    @Column(unique = true)
    private String claimNumber;

    @Enumerated(EnumType.STRING)
    private ClaimType claimType;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ClaimStatus status;

    private BigDecimal approvedAmount;

    @Column(length = 1000)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private MyUser customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claims_officer_id")
    private MyUser claimsOfficer;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL)
    private List<ClaimDocument> documents;

    // Bank details for payouts
    private String bankAccountNumber;
    private String ifscCode;
    private String accountHolderName;
}
