package org.thehartford.willowshield.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thehartford.willowshield.dto.ReadClaimDTO;
import org.thehartford.willowshield.entity.ClaimDocument;
import org.thehartford.willowshield.entity.Claims;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.enums.ClaimStatus;
import org.thehartford.willowshield.repository.ClaimsRepository;
import org.thehartford.willowshield.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.thehartford.willowshield.dto.ApproveClaimRequest;
import org.thehartford.willowshield.exceptions.*;
import java.util.stream.Collectors;

@Service

@Transactional
public class ClaimsOfficerService {

    @Autowired
    private ClaimsRepository claimsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public List<ReadClaimDTO> getAssignedClaims(Long officerId) {
        // Validate officer exists
        MyUser officer = userRepository.findById(officerId)
                .orElseThrow(() -> new UserNotFoundException(officerId));

        // Fetch claims assigned to this officer
        List<Claims> assignedClaims = claimsRepository.findByClaimsOfficer_Id(officerId);

        return assignedClaims.stream()
                .map(this::mapToClaimDTO)
                .collect(Collectors.toList());
    }

    public void approveClaim(Integer claimId, Long officerId, ApproveClaimRequest request) {
        Claims claim = getClaimAndVerifyAssignment(claimId, officerId);

        ClaimCalculationResult result = calculateClaimResult(claim, request);

        claim.setStatus(org.thehartford.willowshield.enums.ClaimStatus.APPROVED);
        claim.setApprovedAmount(result.payoutAmount);
        claimsRepository.save(claim);

        if (result.closePolicy) {
            claim.getPolicy().setStatus(org.thehartford.willowshield.enums.PolicyStatus.INACTIVE);
        }

        notificationService.createNotification(claim.getCustomer(), 
            "Your claim " + claim.getClaimNumber() + " has been approved for amount: " + claim.getApprovedAmount(), 
            "CLAIM_APPROVED");
            
        notificationService.notifyAdmins(
            "Claim " + claim.getClaimNumber() + " has been approved by Claims Officer. Payout amount: " + claim.getApprovedAmount() + ". Admin action required for payment.", 
            "CLAIM_PAYMENT_PENDING");
    }

    public BigDecimal calculatePayment(Integer claimId, Long officerId, ApproveClaimRequest request) {
        Claims claim = getClaimAndVerifyAssignment(claimId, officerId);
        ClaimCalculationResult result = calculateClaimResult(claim, request);
        return result.payoutAmount;
    }

    private static class ClaimCalculationResult {
        BigDecimal payoutAmount;
        boolean closePolicy;

        ClaimCalculationResult(BigDecimal payoutAmount, boolean closePolicy) {
            this.payoutAmount = payoutAmount;
            this.closePolicy = closePolicy;
        }
    }

    private ClaimCalculationResult calculateClaimResult(Claims claim, ApproveClaimRequest request) {
        BigDecimal bill = request.getBillAmount() != null ? request.getBillAmount() : BigDecimal.ZERO;
        BigDecimal exShowroomPrice = request.getExShowroomPrice() != null ? request.getExShowroomPrice()
                : BigDecimal.ZERO;
        Integer yom = request.getYearOfManufacture();

        if (claim.getPolicy() == null || claim.getPolicy().getPlan() == null) {
            throw new InvalidStateException("Policy or Plan details missing for this claim.");
        }

        String policyType = claim.getPolicy().getPlan().getPolicyType().toUpperCase();
        BigDecimal baseCoverage = claim.getPolicy().getPlan().getMaxCoverageAmount() != null
                ? claim.getPolicy().getPlan().getMaxCoverageAmount()
                : BigDecimal.ZERO;

        BigDecimal deductible = claim.getPolicy().getPlan().getDeductibleAmount() != null
                ? claim.getPolicy().getPlan().getDeductibleAmount()
                : BigDecimal.ZERO;

        org.thehartford.willowshield.enums.ClaimType claimType = claim.getClaimType();

        int currentYear = java.time.LocalDate.now().getYear();
        BigDecimal idvDepreciationRate = calculateIdvDepreciationRate(currentYear, yom);
        BigDecimal partsDepreciationRate = calculatePartsDepreciationRate(currentYear, yom);
        BigDecimal idv = exShowroomPrice.multiply(BigDecimal.ONE.subtract(idvDepreciationRate));
        BigDecimal depreciatedBill = bill.multiply(BigDecimal.ONE.subtract(partsDepreciationRate));

        BigDecimal payoutAmount = BigDecimal.ZERO;
        boolean closePolicy = false;

        if (policyType.contains("THIRD_PARTY") && !policyType.contains("COMPREHENSIVE")) {
            if (depreciatedBill.compareTo(idv) > 0) {
                payoutAmount = idv.compareTo(baseCoverage) <= 0 ? idv : baseCoverage;
            } else {
                payoutAmount = depreciatedBill;
            }
        } else if (policyType.contains("COMPREHENSIVE")) {
            if (claimType == org.thehartford.willowshield.enums.ClaimType.THEFT) {
                payoutAmount = idv;
                closePolicy = true;
            } else if (claimType == org.thehartford.willowshield.enums.ClaimType.DAMAGE) {
                BigDecimal threshold = idv.multiply(new BigDecimal("0.75"));
                if (depreciatedBill.compareTo(threshold) > 0) {
                    payoutAmount = idv;
                    closePolicy = true;
                } else {
                    payoutAmount = depreciatedBill;
                }
            } else if (claimType == org.thehartford.willowshield.enums.ClaimType.THIRD_PARTY) {
                payoutAmount = depreciatedBill.compareTo(baseCoverage) > 0 ? baseCoverage : depreciatedBill;
            }
        } else if (policyType.contains("ZERO_DEPRECIATION") || policyType.contains("ZERO_DEP")) {
            if (claimType == org.thehartford.willowshield.enums.ClaimType.THEFT) {
                payoutAmount = idv;
                closePolicy = true;
            } else if (claimType == org.thehartford.willowshield.enums.ClaimType.DAMAGE) {
                BigDecimal threshold = idv.multiply(new BigDecimal("0.75"));
                if (depreciatedBill.compareTo(threshold) > 0) {
                    payoutAmount = idv;
                    closePolicy = true;
                } else {
                    payoutAmount = bill; // no depreciation
                }
            } else if (claimType == org.thehartford.willowshield.enums.ClaimType.THIRD_PARTY) {
                payoutAmount = depreciatedBill.compareTo(baseCoverage) > 0 ? baseCoverage : depreciatedBill;
            }
        } else {
            payoutAmount = depreciatedBill;
        }

        payoutAmount = payoutAmount.subtract(deductible);

        if (payoutAmount.compareTo(BigDecimal.ZERO) < 0) {
            payoutAmount = BigDecimal.ZERO;
        }

        return new ClaimCalculationResult(payoutAmount, closePolicy);
    }

    private BigDecimal calculateIdvDepreciationRate(int currentYear, Integer yearOfManufacture) {
        if (yearOfManufacture == null)
            return BigDecimal.ZERO;
        int age = currentYear - yearOfManufacture;
        if (age <= 0)
            return new BigDecimal("0.05"); // Up to 6 months
        if (age == 1)
            return new BigDecimal("0.15"); // 6 months - 1 year
        if (age == 2)
            return new BigDecimal("0.20"); // 1 - 2 years
        if (age == 3)
            return new BigDecimal("0.30"); // 2 - 3 years
        if (age == 4)
            return new BigDecimal("0.40"); // 3 - 4 years
        if (age == 5)
            return new BigDecimal("0.50"); // 4 - 5 years
        return new BigDecimal("0.70"); // Above 5 years
    }

    private BigDecimal calculatePartsDepreciationRate(int currentYear, Integer yearOfManufacture) {
        if (yearOfManufacture == null)
            return BigDecimal.ZERO;
        int age = currentYear - yearOfManufacture;
        if (age <= 0)
            return new BigDecimal("0.25"); // Up to 6 months
        if (age == 1)
            return new BigDecimal("0.30"); // 6 months - 1 year
        if (age == 2)
            return new BigDecimal("0.32"); // 1 - 2 years
        if (age == 3)
            return new BigDecimal("0.35"); // 2 - 3 years
        if (age == 4)
            return new BigDecimal("0.38"); // 3 - 4 years
        if (age == 5)
            return new BigDecimal("0.40"); // 4 - 5 years
        return new BigDecimal("0.50"); // Above 5 years
    }

    public void rejectClaim(Integer claimId, Long officerId, String reason) {
        Claims claim = getClaimAndVerifyAssignment(claimId, officerId);

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setRejectionReason(reason);
        claimsRepository.save(claim);

        notificationService.createNotification(claim.getCustomer(), 
            "Your claim " + claim.getClaimNumber() + " has been rejected. Reason: " + reason, 
            "CLAIM_REJECTED");
    }

    private Claims getClaimAndVerifyAssignment(Integer claimId, Long officerId) {
        Claims claim = claimsRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        if (claim.getClaimsOfficer() == null || !claim.getClaimsOfficer().getId().equals(officerId)) {
            throw new UnauthorizedAccessException("You are not authorized to process this claim. It is not assigned to you.");
        }

        if (claim.getStatus() != ClaimStatus.ASSIGNED) {
            throw new ClaimAlreadyProcessedException(claimId, claim.getStatus().toString());
        }

        return claim;
    }

    private ReadClaimDTO mapToClaimDTO(Claims claim) {
        ReadClaimDTO dto = new ReadClaimDTO();
        dto.setClaimId(claim.getClaimId());
        dto.setClaimNumber(claim.getClaimNumber());
        dto.setClaimType(claim.getClaimType());
        dto.setStatus(claim.getStatus());
        dto.setApprovedAmount(claim.getApprovedAmount());

        if (claim.getPolicy() != null) {
            dto.setPolicyId(claim.getPolicy().getPolicyId());
            dto.setPolicyNumber(claim.getPolicy().getPolicyNumber());
        }

        if (claim.getCustomer() != null) {
            dto.setCustomerName(claim.getCustomer().getUsername());
        }

        if (claim.getClaimsOfficer() != null) {
            dto.setClaimsOfficerId(claim.getClaimsOfficer().getId());
            dto.setClaimsOfficerName(claim.getClaimsOfficer().getUsername());
        }

        if (claim.getDocuments() != null && !claim.getDocuments().isEmpty()) {
            ClaimDocument docs = claim.getDocuments().get(0);
            dto.setDocument1Path(docs.getDocument1());
            dto.setDocument2Path(docs.getDocument2());
            dto.setDocument3Path(docs.getDocument3());
        }

        dto.setRejectionReason(claim.getRejectionReason());

        return dto;
    }
}
