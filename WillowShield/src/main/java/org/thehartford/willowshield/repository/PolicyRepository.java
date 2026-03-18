package org.thehartford.willowshield.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thehartford.willowshield.entity.Policy;

import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Integer> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"plan", "customer", "vehicle"})
    List<Policy> findByCustomer_Id(Long customerId);

    java.util.Optional<Policy> findByVehicle_VehicleApplication_VehicleApplicationId(Integer applicationId);
}
