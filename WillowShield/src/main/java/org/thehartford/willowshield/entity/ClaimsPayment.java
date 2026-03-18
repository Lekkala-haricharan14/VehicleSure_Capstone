package org.thehartford.willowshield.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.entity.Claims;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims_payment")
@Getter
@Setter
public class ClaimsPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentId;

    @OneToOne
    @JoinColumn(name = "claim_id", nullable = false)
    private Claims claim;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private MyUser admin;

    @Column(nullable = false)
    private BigDecimal amountPaid;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private String paymentStatus;

    @Column(nullable = false)
    private String transactionReference;

    private String bankAccountNumber;
    private String ifscCode;
    private String accountHolderName;

    @PrePersist
    protected void onCreate() {
        this.paymentDate = LocalDateTime.now();
    }
}
