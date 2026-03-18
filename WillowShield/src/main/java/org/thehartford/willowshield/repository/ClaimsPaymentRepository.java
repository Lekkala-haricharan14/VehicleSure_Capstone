package org.thehartford.willowshield.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thehartford.willowshield.entity.ClaimsPayment;
import org.thehartford.willowshield.entity.Claims;

@Repository
public interface ClaimsPaymentRepository extends JpaRepository<ClaimsPayment, Integer> {
    boolean existsByClaim(Claims claim);
}
