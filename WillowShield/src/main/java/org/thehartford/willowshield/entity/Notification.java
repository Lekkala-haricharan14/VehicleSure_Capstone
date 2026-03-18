package org.thehartford.willowshield.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private MyUser user;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private String type; // e.g., POLICY_UPDATE, CLAIM_UPDATE, PAYMENT_SUCCESS, NEW_ASSIGNMENT

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Helper constructor
    public Notification(MyUser user, String message, String type) {
        this.user = user;
        this.message = message;
        this.type = type;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }
}
