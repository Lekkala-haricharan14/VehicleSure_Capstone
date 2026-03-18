package org.thehartford.willowshield.entity;

import org.thehartford.willowshield.enums.PolicyStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "policies")
@Getter
@Setter
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer policyId;

    @Column(nullable = false, unique = true)
    private String policyNumber;

    @Enumerated(EnumType.STRING)
    private PolicyStatus status;

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal premiumAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private MyUser customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "underwriter_id")
    private MyUser underwriter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private PolicyPlan plan;

    @OneToMany(mappedBy = "policy")
    private List<Payment> payments;

    @OneToMany(mappedBy = "policy")
    private List<Claims> claims;

    @Column(columnDefinition = "TEXT")
    private String invoicePath;

    @Column(columnDefinition = "TEXT")
    private String policyDocumentPath;
}
