package org.thehartford.willowshield.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thehartford.willowshield.dto.CreateClaimDTO;
import org.thehartford.willowshield.dto.ReadClaimDTO;
import org.thehartford.willowshield.entity.*;
import org.thehartford.willowshield.enums.ClaimStatus;
import org.thehartford.willowshield.exceptions.InvalidStateException;
import org.thehartford.willowshield.exceptions.ResourceNotFoundException;
import org.thehartford.willowshield.repository.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    @Autowired
    private ClaimsRepository claimsRepository;

    @Autowired
    private ClaimDocumentRepository claimDocumentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private NotificationService notificationService;

    @Value("${file.upload-dir:uploads/claims/}")
    private String uploadDir;

    public ReadClaimDTO submitClaim(Long customerId, CreateClaimDTO dto,
            MultipartFile doc1, MultipartFile doc2, MultipartFile doc3) throws IOException {

        MyUser customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Policy policy = policyRepository.findById(dto.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "id", dto.getPolicyId()));

        // Check for existing active/pending claims for this policy
        List<ClaimStatus> blockingStatuses = Arrays.asList(
                ClaimStatus.SUBMITTED,
                ClaimStatus.ASSIGNED,
                ClaimStatus.APPROVED
        );
        if (claimsRepository.existsByPolicy_PolicyIdAndStatusIn(policy.getPolicyId(), blockingStatuses)) {
            throw new InvalidStateException("A claim for this policy is already under review. You cannot submit another claim until the current claim is resolved.");
        }

        Claims claim = new Claims();
        claim.setCustomer(customer);
        claim.setPolicy(policy);
        claim.setClaimType(dto.getClaimType());
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setClaimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        // Save bank details
        claim.setBankAccountNumber(dto.getBankAccountNumber());
        claim.setIfscCode(dto.getIfscCode());
        claim.setAccountHolderName(dto.getAccountHolderName());

        Claims savedClaim = claimsRepository.save(claim);

        ClaimDocument claimDoc = new ClaimDocument();
        claimDoc.setClaim(savedClaim);

        if (doc1 != null && !doc1.isEmpty()) {
            claimDoc.setDocument1(saveFile(doc1, savedClaim.getClaimId(), "DOC1"));
        }
        if (doc2 != null && !doc2.isEmpty()) {
            claimDoc.setDocument2(saveFile(doc2, savedClaim.getClaimId(), "DOC2"));
        }
        if (doc3 != null && !doc3.isEmpty()) {
            claimDoc.setDocument3(saveFile(doc3, savedClaim.getClaimId(), "DOC3"));
        }

        claimDocumentRepository.save(claimDoc);
        
        notificationService.notifyAdmins(
            "New claim " + savedClaim.getClaimNumber() + " submitted by " + customer.getUsername(), 
            "NEW_CLAIM_SUBMITTED");

        return mapToDTO(savedClaim, claimDoc);
    }

    public List<ReadClaimDTO> getCustomerClaims(Long customerId) {
        return claimsRepository.findByCustomer_Id(customerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ReadClaimDTO> getAllClaims() {
        return claimsRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private String saveFile(MultipartFile file, Integer claimId, String type) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String uniqueFileName = "claim_" + claimId + "_" + type + extension;
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uploadDir + uniqueFileName;
    }

    private ReadClaimDTO mapToDTO(Claims claim) {
        ClaimDocument doc = claim.getDocuments().stream().findFirst().orElse(null);
        return mapToDTO(claim, doc);
    }

    private ReadClaimDTO mapToDTO(Claims claim, ClaimDocument doc) {
        ReadClaimDTO dto = new ReadClaimDTO();
        dto.setClaimId(claim.getClaimId());
        dto.setClaimNumber(claim.getClaimNumber());
        dto.setClaimType(claim.getClaimType());
        dto.setStatus(claim.getStatus());
        dto.setApprovedAmount(claim.getApprovedAmount());
        dto.setPolicyId(claim.getPolicy().getPolicyId());
        dto.setPolicyNumber(claim.getPolicy().getPolicyNumber());
        dto.setCustomerName(claim.getCustomer().getUsername());
        if (doc != null) {
            dto.setDocument1Path(doc.getDocument1());
            dto.setDocument2Path(doc.getDocument2());
            dto.setDocument3Path(doc.getDocument3());
        }
        
        dto.setBankAccountNumber(claim.getBankAccountNumber());
        dto.setIfscCode(claim.getIfscCode());
        dto.setAccountHolderName(claim.getAccountHolderName());
        
        return dto;
    }
}
