package org.thehartford.willowshield.service;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.thehartford.willowshield.dto.*;
import org.thehartford.willowshield.entity.*;
import org.thehartford.willowshield.enums.*;
import org.thehartford.willowshield.exceptions.*;
import org.thehartford.willowshield.repository.*;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
public class AdminService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PolicyPlanRepository policyPlanRepository;

    @Autowired
    private VehicleApplicationRepository vehicleApplicationRepository;

    @Autowired
    private ClaimsRepository claimsRepository;

    @Autowired
    private ClaimsPaymentRepository claimsPaymentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailHubService emailHubService;

    @Autowired
    private RazorpayService razorpayService;

    public void createStaff(CreateStaffDTO dto) {
        String rawPassword = dto.getPassword();
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = generateRandomPassword();
        }

        MyUser user = new MyUser();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(dto.getRole());
        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setActive(true);
        userRepository.save(user);

        // Send email with credentials
        emailHubService.sendStaffWelcomeEmail(dto.getEmail(), dto.getUsername(), rawPassword, dto.getRole().name());
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public ReadStaffDTO deactivateStaff(Long id) {
        MyUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(false);
        return mapToStaffDTO(userRepository.save(user));
    }

    public ReadStaffDTO activateStaff(Long id) {
        MyUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(true);
        return mapToStaffDTO(userRepository.save(user));
    }

    public List<ReadStaffDTO> getAllStaff() {
        return userRepository.findAll().stream()
                .filter(u -> !u.getRole().equals(UserRole.CUSTOMER) && !u.getRole().equals(UserRole.ADMIN))
                .map(this::mapToStaffDTO)
                .collect(Collectors.toList());
    }

    public ReadStaffDTO getStaffById(Long id) {
        MyUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return mapToStaffDTO(user);
    }

    private ReadStaffDTO mapToStaffDTO(MyUser user) {
        ReadStaffDTO dto = new ReadStaffDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setActive(user.isActive());
        return dto;
    }

    public PolicyPlan addPolicyPlan(@Valid CreatePolicyPlanDTO dto) {
        PolicyPlan plan = new PolicyPlan();
        plan.setPlanName(dto.getPlanName());
        plan.setPolicyType(dto.getPolicyType());
        plan.setDescription(dto.getDescription());
        plan.setBasePremium(dto.getBasePremium());
        plan.setMaxCoverageAmount(dto.getMaxCoverageAmount());
        plan.setPolicyDurationMonths(dto.getPolicyDurationMonths());
        plan.setCoversThirdParty(dto.isCoversThirdParty());
        plan.setCoversOwnDamage(dto.isCoversOwnDamage());
        plan.setCoversTheft(dto.isCoversTheft());
        plan.setCoversNaturalDisaster(dto.isCoversNaturalDisaster());
        plan.setZeroDepreciationAvailable(dto.isZeroDepreciationAvailable());
        plan.setEngineProtectionAvailable(dto.isEngineProtectionAvailable());
        plan.setRoadsideAssistanceAvailable(dto.isRoadsideAssistanceAvailable());
        plan.setApplicableVehicleType(dto.getApplicableVehicleType());
        plan.setDeductibleAmount(dto.getDeductibleAmount());
        plan.setActive(true);
        return policyPlanRepository.save(plan);
    }

    public List<ReadPolicyPlanDTO> getAllPolicyPlans() {
        return policyPlanRepository.findAll().stream()
                .map(this::mapToPolicyPlanDTO)
                .collect(Collectors.toList());
    }

    public ReadPolicyPlanDTO getPolicyPlanById(Integer id) {
        PolicyPlan plan = policyPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy Plan not found"));
        return mapToPolicyPlanDTO(plan);
    }

    public ReadPolicyPlanDTO deactivatePolicyPlan(Integer id) {
        PolicyPlan plan = policyPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy Plan", "id", id));
        plan.setActive(false);
        return mapToPolicyPlanDTO(policyPlanRepository.save(plan));
    }

    public ReadPolicyPlanDTO activatePolicyPlan(Integer id) {
        PolicyPlan plan = policyPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy Plan", "id", id));
        plan.setActive(true);
        return mapToPolicyPlanDTO(policyPlanRepository.save(plan));
    }

    private ReadPolicyPlanDTO mapToPolicyPlanDTO(PolicyPlan plan) {
        ReadPolicyPlanDTO dto = new ReadPolicyPlanDTO();
        dto.setPlanId(plan.getPlanId());
        dto.setPlanName(plan.getPlanName());
        dto.setPolicyType(plan.getPolicyType());
        dto.setDescription(plan.getDescription());
        dto.setBasePremium(plan.getBasePremium());
        dto.setMaxCoverageAmount(plan.getMaxCoverageAmount());
        dto.setPolicyDurationMonths(plan.getPolicyDurationMonths());
        dto.setCoversThirdParty(plan.isCoversThirdParty());
        dto.setCoversOwnDamage(plan.isCoversOwnDamage());
        dto.setCoversTheft(plan.isCoversTheft());
        dto.setCoversNaturalDisaster(plan.isCoversNaturalDisaster());
        dto.setZeroDepreciationAvailable(plan.isZeroDepreciationAvailable());
        dto.setEngineProtectionAvailable(plan.isEngineProtectionAvailable());
        dto.setRoadsideAssistanceAvailable(plan.isRoadsideAssistanceAvailable());
        dto.setApplicableVehicleType(plan.getApplicableVehicleType());
        dto.setDeductibleAmount(plan.getDeductibleAmount());
        dto.setActive(plan.isActive());
        return dto;
    }

    public List<ReadVehicleApplicationDTO> getAllApplications() {
        return vehicleApplicationRepository.findAll().stream()
                .map(this::mapToApplicationDTO)
                .collect(Collectors.toList());
    }

    public ReadVehicleApplicationDTO updateApplicationStatus(Integer id, UpdateApplicationStatusDTO dto) {
        VehicleApplication app = vehicleApplicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
        app.setStatus(dto.getStatus());
        return mapToApplicationDTO(vehicleApplicationRepository.save(app));
    }

    public List<UnderwriterWorkloadDTO> getUnderwritersByWorkload() {
        return userRepository.findByRoleAndIsActive(UserRole.UNDERWRITER, true).stream()
                .map(user -> new UnderwriterWorkloadDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        vehicleApplicationRepository.countByAssignedUnderwriterAndStatus(user,
                                VehicleApplicationStatus.ASSIGNED)))
                .collect(Collectors.toList());
    }

    public ReadVehicleApplicationDTO assignApplication(Integer appId, Long underwriterId) {
        VehicleApplication app = vehicleApplicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationNotFoundException(appId));
        MyUser underwriter = userRepository.findById(underwriterId)
                .orElseThrow(() -> new UserNotFoundException(underwriterId));
        if (app.getStatus() != VehicleApplicationStatus.UNDER_REVIEW) {
            throw new InvalidStateException("Application is already " + app.getStatus());
        }
        app.setAssignedUnderwriter(underwriter);
        app.setStatus(VehicleApplicationStatus.ASSIGNED);
        
        notificationService.createNotification(underwriter, 
            "A new policy application has been assigned to you: " + app.getRegistrationNumber(), 
            "NEW_ASSIGNMENT");
            
        return mapToApplicationDTO(vehicleApplicationRepository.save(app));
    }

    private ReadVehicleApplicationDTO mapToApplicationDTO(VehicleApplication app) {
        ReadVehicleApplicationDTO dto = new ReadVehicleApplicationDTO();
        dto.setVehicleApplicationId(app.getVehicleApplicationId());
        dto.setRegistrationNumber(app.getRegistrationNumber());
        dto.setStatus(app.getStatus());
        dto.setCreatedAt(app.getCreatedAt());
        dto.setCalculatedPremium(app.getCalculatedPremium());
        dto.setVehicleOwnerName(app.getVehicleOwnerName());
        dto.setModel(app.getModel());
        dto.setMake(app.getMake());
        dto.setYear(app.getYear());
        dto.setFuelType(app.getFuelType());
        dto.setChassisNumber(app.getChassisNumber());
        dto.setDistanceDriven(app.getDistanceDriven());
        dto.setExShowroomPrice(app.getExShowroomPrice());
        dto.setIdv(app.getIdv());
        dto.setTenureYears(app.getTenureYears());
        dto.setVehicleType(app.getVehicleType());

        if (app.getPlan() != null) {
            dto.setPlanId(app.getPlan().getPlanId());
            dto.setPlanName(app.getPlan().getPlanName());
            dto.setPolicyType(app.getPlan().getPolicyType());
            dto.setDescription(app.getPlan().getDescription());
            dto.setBasePremium(app.getPlan().getBasePremium());
        }
        if (app.getCustomer() != null) {
            dto.setCustomerName(app.getCustomer().getUsername());
            dto.setCustomerEmail(app.getCustomer().getEmail());
        }
        if (app.getAssignedUnderwriter() != null) {
            dto.setAssignedUnderwriterId(app.getAssignedUnderwriter().getId());
            dto.setAssignedUnderwriterName(app.getAssignedUnderwriter().getUsername());
        }

        VehicleDocument doc = app.getDocuments() != null ? app.getDocuments().stream().findFirst().orElse(null) : null;
        if (doc != null) {
            dto.setRcDocumentPath(doc.getRcDocumentPath());
            dto.setInvoiceDocumentPath(doc.getInvoiceDocumentPath());
        }

        return dto;
    }

    // Claims Management
    public List<ClaimsOfficerWorkloadDTO> getClaimsOfficerWorkload() {
        return userRepository.findByRoleAndIsActive(UserRole.CLAIMS_OFFICER, true).stream()
                .map(user -> new ClaimsOfficerWorkloadDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        claimsRepository.countByClaimsOfficerAndStatus(user, ClaimStatus.ASSIGNED)))
                .collect(Collectors.toList());
    }

    public List<ReadClaimDTO> getAllClaims() {
        return claimsRepository.findAll().stream()
                .map(this::mapToClaimDTO)
                .collect(Collectors.toList());
    }

    public ReadClaimDTO assignClaim(Integer claimId, Long officerId) {
        Claims claim = claimsRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        MyUser officer = userRepository.findById(officerId)
                .orElseThrow(() -> new UserNotFoundException(officerId));

        if (!officer.getRole().equals(UserRole.CLAIMS_OFFICER)) {
            throw new InvalidStateException("User is not a Claims Officer");
        }

        if (claim.getStatus() != ClaimStatus.SUBMITTED) {
            throw new InvalidStateException("Claim is already " + claim.getStatus());
        }
        claim.setClaimsOfficer(officer);
        claim.setStatus(ClaimStatus.ASSIGNED);

        notificationService.createNotification(officer, 
            "A new claim has been assigned to you: " + claim.getClaimNumber(), 
            "NEW_ASSIGNMENT");

        return mapToClaimDTO(claimsRepository.save(claim));
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
            MyUser officer = claim.getClaimsOfficer();
            String officerName = (officer.getFullName() != null && !officer.getFullName().isBlank()) 
                                 ? officer.getFullName() 
                                 : officer.getUsername();
            dto.setClaimsOfficerName(officerName);
        }

        if (claim.getDocuments() != null && !claim.getDocuments().isEmpty()) {
            ClaimDocument docs = claim.getDocuments().get(0);
            dto.setDocument1Path(docs.getDocument1());
            dto.setDocument2Path(docs.getDocument2());
            dto.setDocument3Path(docs.getDocument3());
        }

        return dto;
    }

    public List<ReadClaimDTO> getPendingPayments() {
        return claimsRepository.findAll().stream()
                .filter(c -> c.getStatus() == ClaimStatus.APPROVED)
                .filter(c -> !claimsPaymentRepository.existsByClaim(c))
                .map(this::mapToClaimDTO)
                .collect(Collectors.toList());
    }

    public void processClaimPayment(Integer claimId, Long adminId) {
        Claims claim = claimsRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new InvalidStateException("Claim is not in APPROVED status");
        }

        MyUser admin = userRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException(adminId));

        ClaimsPayment payment = new ClaimsPayment();
        payment.setClaim(claim);
        payment.setAdmin(admin);
        payment.setAmountPaid(claim.getApprovedAmount());
        
        // Save bank details used for this specific payout
        payment.setBankAccountNumber(claim.getBankAccountNumber());
        payment.setIfscCode(claim.getIfscCode());
        payment.setAccountHolderName(claim.getAccountHolderName());
        
        try {
            // Trigger Razorpay Payout
            String transactionId = razorpayService.createPayout(claim);
            payment.setTransactionReference(transactionId);
            payment.setPaymentStatus("COMPLETED");
        } catch (Exception e) {
            // In a real system, you might set it to FAILED or PENDING_VERIFICATION
            payment.setTransactionReference("FAILED: " + e.getMessage());
            payment.setPaymentStatus("FAILED");
            throw new BusinessException("Razorpay Payout failed: " + e.getMessage());
        }

        claimsPaymentRepository.save(payment);

        claim.setStatus(ClaimStatus.SETTLED);
        claimsRepository.save(claim);

        notificationService.createNotification(claim.getCustomer(),
            "Your claim " + claim.getClaimNumber() + " has been settled. Amount: " + claim.getApprovedAmount(),
            "CLAIM_SETTLED");
    }

    public List<ReadPaymentDTO> getAllReceivedPayments() {
        return paymentRepository.findAll().stream().map(payment -> {
            ReadPaymentDTO dto = new ReadPaymentDTO();
            dto.setPaymentId(payment.getPaymentId());
            dto.setAmount(payment.getAmount());
            dto.setPaymentDate(payment.getPaymentDate());
            dto.setStatus(payment.getStatus() != null ? payment.getStatus().name() : "UNKNOWN");
            dto.setTransactionReference(payment.getTransactionReference());
            
            Policy policy = payment.getPolicy();
            if (policy != null) {
                dto.setPolicyNumber(policy.getPolicyNumber());
                if (policy.getCustomer() != null) {
                    dto.setCustomerName(policy.getCustomer().getFullName());
                    dto.setCustomerEmail(policy.getCustomer().getEmail());
                }
                
                if (policy.getVehicle() != null) {
                    dto.setVehicleType(policy.getVehicle().getVehicleType() != null ? 
                        policy.getVehicle().getVehicleType().name() : null);
                }
                
                if (policy.getPlan() != null) {
                    dto.setPolicyType(policy.getPlan().getPolicyType());
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    public List<ReadClaimsPaymentDTO> getAllClaimPayouts() {
        return claimsPaymentRepository.findAll().stream().map(payment -> {
            ReadClaimsPaymentDTO dto = new ReadClaimsPaymentDTO();
            dto.setPaymentId(payment.getPaymentId());
            
            if (payment.getClaim() != null) {
                dto.setClaimId(payment.getClaim().getClaimId());
                dto.setClaimNumber(payment.getClaim().getClaimNumber());
                Policy policy = payment.getClaim().getPolicy();
                if (policy != null) {
                    dto.setPolicyNumber(policy.getPolicyNumber());
                    if (policy.getCustomer() != null) {
                        dto.setCustomerName(policy.getCustomer().getFullName());
                        dto.setCustomerEmail(policy.getCustomer().getEmail());
                    }
                    
                    if (policy.getVehicle() != null) {
                        dto.setVehicleType(policy.getVehicle().getVehicleType() != null ? 
                            policy.getVehicle().getVehicleType().name() : null);
                    }
                    
                    if (policy.getPlan() != null) {
                        dto.setPolicyType(policy.getPlan().getPolicyType());
                    }
                }
                
                if (payment.getClaim().getClaimsOfficer() != null) {
                    MyUser officer = payment.getClaim().getClaimsOfficer();
                    String officerName = (officer.getFullName() != null && !officer.getFullName().isBlank()) 
                                         ? officer.getFullName() 
                                         : officer.getUsername();
                    dto.setClaimsOfficerName(officerName);
                    dto.setClaimsOfficerEmail(officer.getEmail());
                }
            }

            dto.setAmountPaid(payment.getAmountPaid());
            dto.setPaymentDate(payment.getPaymentDate());
            dto.setPaymentStatus(payment.getPaymentStatus());
            dto.setTransactionReference(payment.getTransactionReference());
            if (payment.getAdmin() != null) {
                dto.setAdminName(payment.getAdmin().getFullName());
            }
            
            dto.setBankAccountNumber(payment.getBankAccountNumber());
            dto.setIfscCode(payment.getIfscCode());
            dto.setAccountHolderName(payment.getAccountHolderName());
            
            return dto;
        }).collect(Collectors.toList());
    }
}
