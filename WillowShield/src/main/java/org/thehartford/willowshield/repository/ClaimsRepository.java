package org.thehartford.willowshield.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thehartford.willowshield.entity.Claims;
import org.thehartford.willowshield.entity.MyUser;
import java.util.List;

@Repository
public interface ClaimsRepository extends JpaRepository<Claims, Integer> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"customer", "policy", "policy.vehicle", "policy.plan"})
    List<Claims> findByCustomer_Id(Long customerId);

    List<Claims> findByClaimsOfficer_Id(Long officerId);

    long countByClaimsOfficerAndStatus(MyUser officer,
            org.thehartford.willowshield.enums.ClaimStatus status);

    boolean existsByPolicy_PolicyIdAndStatusIn(Integer policyId, List<org.thehartford.willowshield.enums.ClaimStatus> statuses);
}
