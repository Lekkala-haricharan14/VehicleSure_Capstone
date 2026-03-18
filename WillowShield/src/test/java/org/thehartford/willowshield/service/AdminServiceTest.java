package org.thehartford.willowshield.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thehartford.willowshield.dto.*;
import org.thehartford.willowshield.entity.*;
import org.thehartford.willowshield.enums.*;
import org.thehartford.willowshield.repository.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PolicyPlanRepository policyPlanRepository;

    @Mock
    private VehicleApplicationRepository vehicleApplicationRepository;

    @Mock
    private ClaimsRepository claimsRepository;

    @Mock
    private ClaimsPaymentRepository claimsPaymentRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AdminService adminService;

    @Test
    void createStaff_Success() {
        CreateStaffDTO dto = new CreateStaffDTO();
        dto.setUsername("staffuser");
        dto.setEmail("staff@test.com");
        dto.setPassword("password");
        dto.setRole(UserRole.UNDERWRITER);

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        adminService.createStaff(dto);

        verify(userRepository, times(1)).save(any(MyUser.class));
    }

    @Test
    void deactivateStaff_Success() {
        MyUser user = new MyUser();
        user.setId(1L);
        user.setActive(true);
        user.setRole(UserRole.UNDERWRITER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(MyUser.class))).thenReturn(user);

        ReadStaffDTO result = adminService.deactivateStaff(1L);

        assertFalse(result.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void addPolicyPlan_Success() {
        CreatePolicyPlanDTO dto = new CreatePolicyPlanDTO();
        dto.setPlanName("Test Plan");
        dto.setBasePremium(BigDecimal.valueOf(1000));

        PolicyPlan plan = new PolicyPlan();
        plan.setPlanId(1);
        plan.setPlanName("Test Plan");

        when(policyPlanRepository.save(any(PolicyPlan.class))).thenReturn(plan);

        PolicyPlan result = adminService.addPolicyPlan(dto);

        assertNotNull(result);
        assertEquals("Test Plan", result.getPlanName());
    }

    @Test
    void assignApplication_Success() {
        VehicleApplication app = new VehicleApplication();
        app.setVehicleApplicationId(1);
        app.setStatus(VehicleApplicationStatus.UNDER_REVIEW);
        app.setRegistrationNumber("DL01AB1234");

        MyUser underwriter = new MyUser();
        underwriter.setId(2L);
        underwriter.setRole(UserRole.UNDERWRITER);

        when(vehicleApplicationRepository.findById(1)).thenReturn(Optional.of(app));
        when(userRepository.findById(2L)).thenReturn(Optional.of(underwriter));
        when(vehicleApplicationRepository.save(any(VehicleApplication.class))).thenReturn(app);

        ReadVehicleApplicationDTO result = adminService.assignApplication(1, 2L);

        assertEquals(VehicleApplicationStatus.ASSIGNED, result.getStatus());
        assertEquals(2L, result.getAssignedUnderwriterId());
        verify(notificationService).createNotification(eq(underwriter), anyString(), eq("NEW_ASSIGNMENT"));
    }

    @Test
    void processClaimPayment_Success() {
        Claims claim = new Claims();
        claim.setClaimId(1);
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setClaimNumber("CLM-123");
        claim.setApprovedAmount(BigDecimal.valueOf(5000));

        MyUser admin = new MyUser();
        admin.setId(1L);

        when(claimsRepository.findById(1)).thenReturn(Optional.of(claim));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        adminService.processClaimPayment(1, 1L);

        verify(claimsPaymentRepository).save(any(ClaimsPayment.class));
        verify(claimsRepository).save(claim);
        assertEquals(ClaimStatus.SETTLED, claim.getStatus());
        verify(notificationService).createNotification(any(), anyString(), eq("CLAIM_SETTLED"));
    }

    @Test
    void processClaimPayment_Failure_NotApproved() {
        Claims claim = new Claims();
        claim.setClaimId(1);
        claim.setStatus(ClaimStatus.SUBMITTED);

        when(claimsRepository.findById(1)).thenReturn(Optional.of(claim));

        assertThrows(InvalidStateException.class, () -> adminService.processClaimPayment(1, 1L));
    }
}
