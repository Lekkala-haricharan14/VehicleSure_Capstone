package org.thehartford.willowshield.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thehartford.willowshield.entity.PolicyPlan;

import java.util.List;
import org.thehartford.willowshield.enums.VehicleType;

public interface PolicyPlanRepository extends JpaRepository<PolicyPlan, Integer> {
    List<PolicyPlan> findByApplicableVehicleType(VehicleType applicableVehicleType);

    List<PolicyPlan> findByApplicableVehicleTypeAndIsActiveTrue(VehicleType applicableVehicleType);

    List<PolicyPlan> findByIsActiveTrue();
}