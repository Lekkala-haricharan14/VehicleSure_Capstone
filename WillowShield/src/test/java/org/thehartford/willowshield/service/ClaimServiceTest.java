package org.thehartford.willowshield.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.thehartford.willowshield.dto.CreateClaimDTO;
import org.thehartford.willowshield.dto.ReadClaimDTO;
import org.thehartford.willowshield.entity.*;
import org.thehartford.willowshield.enums.ClaimStatus;
import org.thehartford.willowshield.enums.ClaimType;
import org.thehartford.willowshield.repository.*;

@ExtendWith(MockitoExtension.class)
public class ClaimServiceTest {

    @Mock
    private ClaimsRepository claimsRepository;

    @Mock
    private ClaimDocumentRepository claimDocumentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ClaimService claimService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(claimService, "uploadDir", "uploads/claims/");
    }

    @Test
    void submitClaim_Success() throws IOException {
        Long customerId = 1L;
        CreateClaimDTO dto = new CreateClaimDTO();
        dto.setPolicyId(1);
        dto.setClaimType(ClaimType.DAMAGE);

        MyUser customer = new MyUser();
        customer.setId(customerId);
        customer.setUsername("testuser");

        Policy policy = new Policy();
        policy.setPolicyId(1);
        policy.setPolicyNumber("POL123");

        Claims savedClaim = new Claims();
        savedClaim.setClaimId(1);
        savedClaim.setClaimNumber("CLM-123");
        savedClaim.setCustomer(customer);
        savedClaim.setPolicy(policy);
        savedClaim.setClaimType(ClaimType.DAMAGE);
        savedClaim.setStatus(ClaimStatus.SUBMITTED);

        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
        when(claimsRepository.existsByPolicy_PolicyIdAndStatusIn(eq(1), anyList())).thenReturn(false);
        when(claimsRepository.save(any(Claims.class))).thenReturn(savedClaim);

        MockMultipartFile doc1 = new MockMultipartFile("doc1", "doc1.pdf", "application/pdf", "content".getBytes());

        ReadClaimDTO result = claimService.submitClaim(customerId, dto, doc1, null, null);

        assertNotNull(result);
        assertEquals("CLM-123", result.getClaimNumber());
        verify(claimsRepository).save(any(Claims.class));
        verify(claimDocumentRepository).save(any(ClaimDocument.class));
        verify(notificationService).notifyAdmins(anyString(), eq("NEW_CLAIM_SUBMITTED"));
    }

    @Test
    void submitClaim_Failure_ExistingClaim() {
        Long customerId = 1L;
        CreateClaimDTO dto = new CreateClaimDTO();
        dto.setPolicyId(1);

        MyUser customer = new MyUser();
        customer.setId(customerId);

        Policy policy = new Policy();
        policy.setPolicyId(1);

        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
        when(claimsRepository.existsByPolicy_PolicyIdAndStatusIn(eq(1), anyList())).thenReturn(true);

        assertThrows(InvalidStateException.class, () -> claimService.submitClaim(customerId, dto, null, null, null));
    }

    @Test
    void getCustomerClaims_Success() {
        Long customerId = 1L;
        Claims claim = new Claims();
        claim.setClaimId(1);
        claim.setCustomer(new MyUser());
        claim.getCustomer().setUsername("testuser");
        claim.setPolicy(new Policy());
        claim.getPolicy().setPolicyId(1);
        claim.getPolicy().setPolicyNumber("POL123");
        claim.setDocuments(Collections.emptyList()); // Fix NPE

        when(claimsRepository.findByCustomer_Id(customerId)).thenReturn(List.of(claim));

        List<ReadClaimDTO> result = claimService.getCustomerClaims(customerId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}
