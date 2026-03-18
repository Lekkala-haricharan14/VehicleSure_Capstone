package org.thehartford.willowshield.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.thehartford.willowshield.enums.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(unique = true, length = 150)
    private String email;

    private String fullName;

    @Column(name = "phone_number", nullable = false, unique = true, length = 15)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean isActive = true;

    // add created at
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Vehicles owned by customer
    @OneToMany(mappedBy = "customer")
    private List<Vehicle> vehicles;

    // Policies as customer
    @OneToMany(mappedBy = "customer")
    private List<Policy> customerPolicies;

    // Policies approved by underwriter
    @OneToMany(mappedBy = "underwriter")
    private List<Policy> underwrittenPolicies;

    // Claims filed by customer
    @OneToMany(mappedBy = "customer")
    private List<Claims> customerClaims;

    // Claims handled by claims officer
    @OneToMany(mappedBy = "claimsOfficer")
    private List<Claims> claimsHandled;

    // Vehicle applications assigned to underwriter
    @OneToMany(mappedBy = "assignedUnderwriter")
    private List<VehicleApplication> assignedApplications;
}
