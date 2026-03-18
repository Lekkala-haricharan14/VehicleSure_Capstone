package org.thehartford.willowshield.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thehartford.willowshield.dto.ApproveClaimRequest;
import org.thehartford.willowshield.dto.ReadClaimDTO;
import org.thehartford.willowshield.entity.Claims;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.entity.Policy;
import org.thehartford.willowshield.entity.PolicyPlan;
import org.thehartford.willowshield.enums.ClaimStatus;
import org.thehartford.willowshield.enums.ClaimType;
import org.thehartford.willowshield.enums.PolicyStatus;
import org.thehartford.willowshield.repository.ClaimsRepository;
import org.thehartford.willowshield.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ClaimsOfficerServiceTest {

    @Mock
    private ClaimsRepository claimsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ClaimsOfficerService claimsOfficerService;

    @Test
    void getAssignedClaims_Success() {
        Long officerId = 1L;
        MyUser officer = new MyUser();
        officer.setId(officerId);
        officer.setUsername("officer1");

        Claims claim = new Claims();
        claim.setClaimId(1);
        claim.setClaimType(ClaimType.DAMAGE);
        claim.setStatus(ClaimStatus.ASSIGNED);
        claim.setClaimsOfficer(officer);

        when(userRepository.findById(officerId)).thenReturn(Optional.of(officer));
        when(claimsRepository.findByClaimsOfficer_Id(officerId)).thenReturn(List.of(claim));

        List<ReadClaimDTO> result = claimsOfficerService.getAssignedClaims(officerId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(officerId, result.get(0).getClaimsOfficerId());
    }

    @Test
    void approveClaim_Success() {
        Integer claimId = 1;
        Long officerId = 2L;

        MyUser officer = new MyUser();
        officer.setId(officerId);

        PolicyPlan plan = new PolicyPlan();
        plan.setPolicyType("COMPREHENSIVE");
        plan.setMaxCoverageAmount(BigDecimal.valueOf(100000));
        plan.setDeductibleAmount(BigDecimal.valueOf(1000));

        Policy policy = new Policy();
        policy.setPlan(plan);
        policy.setStatus(PolicyStatus.ACTIVE);

        Claims claim = new Claims();
        claim.setClaimId(claimId);
        claim.setClaimType(ClaimType.DAMAGE);
        claim.setStatus(ClaimStatus.ASSIGNED);
        claim.setClaimsOfficer(officer);
        claim.setPolicy(policy);

        ApproveClaimRequest request = new ApproveClaimRequest();
        request.setBillAmount(BigDecimal.valueOf(5000));
        request.setExShowroomPrice(BigDecimal.valueOf(800000));
        request.setYearOfManufacture(2022);

        when(claimsRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(claimsRepository.save(any(Claims.class))).thenReturn(claim);

        claimsOfficerService.approveClaim(claimId, officerId, request);

        assertEquals(ClaimStatus.APPROVED, claim.getStatus());
        assertNotNull(claim.getApprovedAmount());
        verify(claimsRepository).save(claim);
        verify(notificationService).createNotification(eq(claim.getCustomer()), anyString(), eq("CLAIM_APPROVED"));
        verify(notificationService).notifyAdmins(anyString(), eq("CLAIM_PAYMENT_PENDING"));
    }

    @Test
    void rejectClaim_Success() {
        Integer claimId = 1;
        Long officerId = 2L;
        String reason = "Fraudulent claim";

        MyUser officer = new MyUser();
        officer.setId(officerId);

        Claims claim = new Claims();
        claim.setClaimId(claimId);
        claim.setStatus(ClaimStatus.ASSIGNED);
        claim.setClaimsOfficer(officer);

        when(claimsRepository.findById(claimId)).thenReturn(Optional.of(claim));

        claimsOfficerService.rejectClaim(claimId, officerId, reason);

        assertEquals(ClaimStatus.REJECTED, claim.getStatus());
        assertEquals(reason, claim.getRejectionReason());
        verify(claimsRepository).save(claim);
        verify(notificationService).createNotification(eq(claim.getCustomer()), anyString(), eq("CLAIM_REJECTED"));
    }
}
