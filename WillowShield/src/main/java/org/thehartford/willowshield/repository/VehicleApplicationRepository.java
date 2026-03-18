package org.thehartford.willowshield.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.entity.VehicleApplication;
import org.thehartford.willowshield.enums.VehicleApplicationStatus;

import java.util.List;

@Repository
public interface VehicleApplicationRepository extends JpaRepository<VehicleApplication, Integer> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"plan", "customer", "assignedUnderwriter"})
    List<VehicleApplication> findByCustomer_Id(Long customerId);

    List<VehicleApplication> findByStatus(VehicleApplicationStatus status);

    long countByAssignedUnderwriterAndStatus(MyUser underwriter, VehicleApplicationStatus status);

    boolean existsByRegistrationNumberAndStatusNot(String registrationNumber, VehicleApplicationStatus status);
}
