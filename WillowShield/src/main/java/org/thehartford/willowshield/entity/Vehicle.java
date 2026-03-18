package org.thehartford.willowshield.entity;

import jakarta.persistence.*;
import lombok.*;
import org.thehartford.willowshield.enums.VehicleType;
import org.thehartford.willowshield.enums.TransmissionType;
import org.thehartford.willowshield.enums.RiskLevel;

import java.util.List;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer vehicleId;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    private String make;
    private String model;
    private Integer year;
    private String fuelType;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private MyUser customer;

    @OneToMany(mappedBy = "vehicle")
    private List<Policy> policies;

    @OneToOne
    @JoinColumn(name = "vehicle_application_id")
    private VehicleApplication vehicleApplication;
}
