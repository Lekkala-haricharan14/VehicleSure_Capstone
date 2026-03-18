package org.thehartford.willowshield.entity;

import org.thehartford.willowshield.enums.VehicleApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.thehartford.willowshield.enums.VehicleType;
import org.thehartford.willowshield.enums.TransmissionType;
import org.thehartford.willowshield.enums.RiskLevel;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "vehicle_applications")
@Getter
@Setter
public class VehicleApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer vehicleApplicationId;
    private String vehicleOwnerName;

    public String getVehicleOwnerName() {
        return vehicleOwnerName;
    }

    public void setVehicleOwnerName(String vehicleOwnerName) {
        this.vehicleOwnerName = vehicleOwnerName;
    }

    private String registrationNumber;
    private String make;
    private String model;
    private Integer year;
    private String fuelType;
    private String chassisNumber;
    private Long distanceDriven;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransmissionType transmissionType;

    @Column(nullable = false)
    private Integer accidentsInPast;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    private BigDecimal exShowroomPrice;
    private BigDecimal idv;
    private BigDecimal calculatedPremium;
    private Integer tenureYears;

    @Enumerated(EnumType.STRING)
    private VehicleApplicationStatus status = VehicleApplicationStatus.UNDER_REVIEW;

    private String rejectionReason;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private MyUser customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_underwriter_id")
    private MyUser assignedUnderwriter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private PolicyPlan plan;

    @OneToMany(mappedBy = "vehicleApplication", cascade = CascadeType.ALL)
    private List<VehicleDocument> documents;
}
