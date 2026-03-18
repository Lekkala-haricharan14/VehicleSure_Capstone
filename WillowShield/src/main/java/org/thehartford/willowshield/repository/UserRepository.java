package org.thehartford.willowshield.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.enums.UserRole;
import org.thehartford.willowshield.dto.UnderwriterWorkloadDTO;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<MyUser, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String username);

    Optional<MyUser> findByEmail(String email);

    List<MyUser> findByRole(UserRole role);

    List<MyUser> findByRoleIn(List<UserRole> roles);

    List<MyUser> findByRoleAndIsActive(UserRole role, boolean isActive);

    @Query("SELECT new org.thehartford.willowshield.dto.UnderwriterWorkloadDTO(u.id, u.username, u.email, COUNT(a.vehicleApplicationId)) "
            +
            "FROM MyUser u LEFT JOIN u.assignedApplications a ON a.status = 'UNDER_REVIEW' " +
            "WHERE u.role = 'UNDERWRITER' AND u.isActive = true " +
            "GROUP BY u.id, u.username, u.email " +
            "ORDER BY COUNT(a.vehicleApplicationId) ASC")

    List<UnderwriterWorkloadDTO> getUnderwritersByWorkload();
}
