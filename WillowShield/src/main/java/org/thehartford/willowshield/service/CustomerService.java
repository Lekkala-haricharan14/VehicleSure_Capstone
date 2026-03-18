package org.thehartford.willowshield.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thehartford.willowshield.dto.CreateVehicleApplicationDTO;
import org.thehartford.willowshield.dto.ReadPolicyDTO;
import org.thehartford.willowshield.dto.ReadVehicleApplicationDTO;
import org.thehartford.willowshield.entity.*;
import org.thehartford.willowshield.enums.VehicleApplicationStatus;
import org.thehartford.willowshield.exceptions.*;
import org.thehartford.willowshield.repository.*;
import org.thehartford.willowshield.dto.ReadPolicyPlanDTO;
import org.thehartford.willowshield.dto.QuoteRequestDTO;
import org.thehartford.willowshield.dto.QuoteResponseDTO;
import org.thehartford.willowshield.dto.QuoteOptionDTO;
import org.thehartford.willowshield.enums.RiskLevel;
import org.thehartford.willowshield.enums.VehicleType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PolicyPlanRepository policyPlanRepository;

    @Autowired
    private VehicleApplicationRepository vehicleApplicationRepository;

    @Autowired
    private VehicleDocumentRepository vehicleDocumentRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private NotificationService notificationService;

    // Define where files will be stored. You can override this in
    // application.properties
    @Value("${file.upload-dir:uploads/documents/}")
    private String uploadDir;

    public void submitVehicleApplication(Long customerId, CreateVehicleApplicationDTO dto, MultipartFile rcFile,
            MultipartFile invoiceFile) throws IOException {

        // Ensure customer exists
        MyUser customer = userRepository.findById(customerId)
                .orElseThrow(() -> new UserNotFoundException(customerId));

        // Ensure policy plan exists and is active
        PolicyPlan plan = policyPlanRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy Plan", "id", dto.getPlanId()));

        if (!plan.isActive()) {
            throw new InvalidStateException("Selected policy plan is not currently active");
        }

        // Check if vehicle type matches
        if (plan.getApplicableVehicleType() != null && plan.getApplicableVehicleType() != dto.getVehicleType()) {
            throw new InvalidStateException("Selected policy plan is not applicable for this vehicle type");
        }

        // 1. Format and Validate Registration Number
        String formattedRegNumber = dto.getRegistrationNumber() != null
                ? dto.getRegistrationNumber().replaceAll("\\s+", "").toUpperCase()
                : "";
                
        if (!formattedRegNumber.matches("^[A-Z]{2}[0-9]{2}[A-Z]{2}[0-9]{4}$")) {
            throw new BusinessException("Invalid Registration Number format. Expected format: XX00XX0000");
        }
        
        // 2. Format and Validate Chassis Number
        String formattedChassis = dto.getChassisNumber() != null
                ? dto.getChassisNumber().toUpperCase()
                : "";

        if (!formattedChassis.matches("^[A-HJ-NPR-Z0-9]{17}$")) {
            throw new BusinessException("Invalid Chassis Number. It must be exactly 17 characters long, containing only numbers and uppercase letters (excluding I, O, Q).");
        }

        // 3. Validate Manufacturing Year
        int currentYear = java.time.Year.now().getValue();
        if (dto.getYear() > currentYear) {
            throw new BusinessException("Manufacturing year cannot be strictly in the future.");
        }

        // 4. Validate Distance Driven
        if (dto.getDistanceDriven() < 0) {
            throw new BusinessException("Distance driven cannot be a negative number.");
        }
        
        // 5. Prevent Duplicate Active Applications
        boolean isDuplicate = vehicleApplicationRepository.existsByRegistrationNumberAndStatusNot(
                formattedRegNumber, VehicleApplicationStatus.REJECTED);
                
        if (isDuplicate) {
            throw new DuplicateResourceException("Application", "registrationNumber", formattedRegNumber);
        }

        // 6. Calculate Risk
        int vehicleAge = Math.max(0, currentYear - dto.getYear());
        RiskLevel calculatedRisk = calculateRiskLevel(vehicleAge, dto.getAccidentsInPast(), dto.getDistanceDriven());

        // 3. Create the base Application
        VehicleApplication application = new VehicleApplication();
        application.setCustomer(customer);
        application.setPlan(plan);
        application.setVehicleOwnerName(dto.getVehicleOwnerName());
        application.setRegistrationNumber(formattedRegNumber);
        application.setMake(dto.getMake());
        application.setModel(dto.getModel());
        application.setYear(dto.getYear());
        application.setFuelType(dto.getFuelType());
        application.setChassisNumber(formattedChassis);  // Uses the formatted uppercase chassis
        application.setDistanceDriven(dto.getDistanceDriven());
        application.setVehicleType(dto.getVehicleType());
        application.setTransmissionType(dto.getTransmissionType());
        application.setAccidentsInPast(dto.getAccidentsInPast());
        application.setRiskLevel(calculatedRisk);
        application.setExShowroomPrice(dto.getExShowroomPrice());
        application.setIdv(dto.getIdv());
        application.setCalculatedPremium(dto.getCalculatedPremium());
        application.setTenureYears(dto.getTenureYears());
        application.setStatus(VehicleApplicationStatus.UNDER_REVIEW);
        application.setCreatedAt(LocalDateTime.now());

        // Save application to generate ID
        VehicleApplication savedApplication = vehicleApplicationRepository.save(application);

        // 2. Handle File Uploads
        String rcPath = saveFile(rcFile, savedApplication.getVehicleApplicationId(), "RC");
        String invoicePath = saveFile(invoiceFile, savedApplication.getVehicleApplicationId(), "Invoice");

        // 3. Create Document Record
        VehicleDocument document = new VehicleDocument();
        document.setVehicleApplication(savedApplication);
        document.setRcDocumentPath(rcPath);
        document.setInvoiceDocumentPath(invoicePath);
        document.setUploadedAt(LocalDateTime.now());

        vehicleDocumentRepository.save(document);
        
        notificationService.notifyAdmins(
            "New policy application submitted by " + customer.getUsername() + " for vehicle " + formattedRegNumber, 
            "NEW_APPLICATION_SUBMITTED");
    }

    public List<ReadVehicleApplicationDTO> getCustomerApplications(Long customerId) {
        List<VehicleApplication> applications = vehicleApplicationRepository.findByCustomer_Id(customerId);
        
        // Fix N+1 queries: Fetch all policies for this customer at once
        List<Policy> customerPolicies = policyRepository.findByCustomer_Id(customerId);
        Map<Integer, Policy> policyMapByAppId = new HashMap<>();
        for (Policy p : customerPolicies) {
            if (p.getVehicle() != null && p.getVehicle().getVehicleApplication() != null) {
                policyMapByAppId.put(p.getVehicle().getVehicleApplication().getVehicleApplicationId(), p);
            }
        }

        return applications.stream().map(app -> {
            ReadVehicleApplicationDTO dto = new ReadVehicleApplicationDTO();
            dto.setVehicleApplicationId(app.getVehicleApplicationId());
            dto.setVehicleOwnerName(app.getVehicleOwnerName());
            dto.setRegistrationNumber(app.getRegistrationNumber());
            dto.setMake(app.getMake());
            dto.setModel(app.getModel());
            dto.setYear(app.getYear());
            dto.setFuelType(app.getFuelType());
            dto.setChassisNumber(app.getChassisNumber());
            dto.setDistanceDriven(app.getDistanceDriven());
            dto.setVehicleType(app.getVehicleType());
            dto.setExShowroomPrice(app.getExShowroomPrice());
            dto.setIdv(app.getIdv());
            dto.setCalculatedPremium(app.getCalculatedPremium());
            dto.setTenureYears(app.getTenureYears());
            dto.setStatus(app.getStatus());
            dto.setRejectionReason(app.getRejectionReason());
            dto.setCreatedAt(app.getCreatedAt());

            if (app.getPlan() != null) {
                dto.setPlanId(app.getPlan().getPlanId());
                dto.setPlanName(app.getPlan().getPlanName());
                dto.setPolicyType(app.getPlan().getPolicyType());
                dto.setDescription(app.getPlan().getDescription());
                dto.setBasePremium(app.getPlan().getBasePremium());
            }

            // Populate policyId if application is APPROVED or PAID using bulk-loaded map
            if (app.getStatus() == VehicleApplicationStatus.APPROVED
                    || app.getStatus() == VehicleApplicationStatus.PAID) {
                Policy policy = policyMapByAppId.get(app.getVehicleApplicationId());
                if (policy != null) {
                    dto.setPolicyId(policy.getPolicyId());
                    dto.setGeneratedInvoicePath(policy.getInvoicePath());
                    dto.setGeneratedPolicyPath(policy.getPolicyDocumentPath());
                }
            }

            return dto;
        }).collect(Collectors.toList());
    }

    public List<ReadPolicyDTO> getCustomerPolicies(Long customerId) {
        List<Policy> policies = policyRepository.findByCustomer_Id(customerId);
       LocalDate today = java.time.LocalDate.now();
        return policies.stream().map(p -> {
            ReadPolicyDTO dto = new ReadPolicyDTO();
            dto.setPolicyId(p.getPolicyId());
            dto.setPolicyNumber(p.getPolicyNumber());
            dto.setStatus(p.getStatus());
            dto.setStartDate(p.getStartDate());
            dto.setEndDate(p.getEndDate());
            dto.setPremiumAmount(p.getPremiumAmount());
            dto.setCustomerId(p.getCustomer().getId());
            dto.setCustomerName(p.getCustomer().getUsername());
            dto.setVehicleId(p.getVehicle().getVehicleId());
            dto.setVehicleRegistrationNumber(p.getVehicle().getRegistrationNumber());
            dto.setPlanId(p.getPlan().getPlanId());
            dto.setPlanName(p.getPlan().getPlanName());
            dto.setPolicyType(p.getPlan().getPolicyType());
            dto.setDescription(p.getPlan().getDescription());
            dto.setMaxCoverageAmount(p.getPlan().getMaxCoverageAmount());
            dto.setDeductibleAmount(p.getPlan().getDeductibleAmount());
            dto.setCoversThirdParty(p.getPlan().isCoversThirdParty());
            dto.setCoversOwnDamage(p.getPlan().isCoversOwnDamage());
            dto.setCoversTheft(p.getPlan().isCoversTheft());
            dto.setCoversNaturalDisaster(p.getPlan().isCoversNaturalDisaster());
            dto.setExpired(today.isAfter(p.getEndDate()));
            return dto;
        }).collect(Collectors.toList());
    }

    private String saveFile(MultipartFile file, Integer applicationId, String docType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException(docType + " file is empty or missing");
        }

        // Create the directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate a unique filename: app_ID_DocType_UUID.extension
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFileName = String.format("app_%d_%s%s",
                applicationId,
                docType,
                extension);

        Path filePath = uploadPath.resolve(uniqueFileName);

        // Copy the file stream to the destination path, replacing if it bizarrely
        // exists
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return the relative path to store in DB
        return uploadDir + uniqueFileName;
    }

    public List<ReadPolicyPlanDTO> getActivePolicyPlans(String typeParam) {
        List<PolicyPlan> activePlans;

        if (typeParam != null && !typeParam.isEmpty()) {
            try {
                VehicleType vType = VehicleType.valueOf(typeParam.toUpperCase());
                activePlans = policyPlanRepository.findByApplicableVehicleTypeAndIsActiveTrue(vType);
            } catch (IllegalArgumentException e) {
                return List.of(); // Invalid vehicle type returns empty
            }
        } else {
            activePlans = policyPlanRepository.findByIsActiveTrue();
        }

        return activePlans.stream().map(plan -> {
            ReadPolicyPlanDTO dto = new ReadPolicyPlanDTO();
            dto.setPlanId(plan.getPlanId());
            dto.setPlanName(plan.getPlanName());
            dto.setPolicyType(plan.getPolicyType());
            dto.setDescription(plan.getDescription());
            dto.setBasePremium(plan.getBasePremium());
            dto.setMaxCoverageAmount(plan.getMaxCoverageAmount());
            dto.setPolicyDurationMonths(plan.getPolicyDurationMonths());
            dto.setDeductibleAmount(plan.getDeductibleAmount());
            dto.setApplicableVehicleType(plan.getApplicableVehicleType());
            dto.setActive(plan.isActive());
            return dto;
        }).collect(Collectors.toList());
    }

    public RiskLevel calculateRiskLevel(int ageYears, int accidentsInPast, long distanceDriven) {
        int riskScore = 0;
        if (ageYears > 5) riskScore += 1;
        riskScore += accidentsInPast;
        if (distanceDriven > 100000) riskScore += 1;

        if (riskScore <= 1) return RiskLevel.LOW;
        if (riskScore == 2) return RiskLevel.MEDIUM;
        return RiskLevel.HIGH;
    }

    public List<QuoteResponseDTO> generateQuotes(QuoteRequestDTO request) {
        int currentYear = LocalDate.now().getYear();
        int ageYears = Math.max(0, currentYear - request.getYear());

        RiskLevel risk = calculateRiskLevel(ageYears, request.getAccidentsInPast(), request.getDistanceDriven());

        // IDV Depreciates 10% per year, max 50%
        BigDecimal depreciation = BigDecimal.valueOf(0.10).multiply(BigDecimal.valueOf(ageYears));
        if (depreciation.compareTo(BigDecimal.valueOf(0.50)) > 0) {
            depreciation = BigDecimal.valueOf(0.50);
        }
        BigDecimal idv = request.getExShowroomPrice().multiply(BigDecimal.ONE.subtract(depreciation));

        List<PolicyPlan> applicablePlans = policyPlanRepository.findByApplicableVehicleTypeAndIsActiveTrue(request.getVehicleType());

        BigDecimal riskMultiplier = BigDecimal.ONE;
        if (risk == RiskLevel.MEDIUM) riskMultiplier = BigDecimal.valueOf(1.2);
        if (risk == RiskLevel.HIGH) riskMultiplier = BigDecimal.valueOf(1.5);

        // IDV Multiplier (Higher IDV = Higher Premium)
        BigDecimal idvMultiplier = BigDecimal.ONE;
        if (idv.compareTo(BigDecimal.valueOf(1000000)) > 0) {
            idvMultiplier = BigDecimal.valueOf(1.3); // +30% for luxury/high-value
        } else if (idv.compareTo(BigDecimal.valueOf(500000)) > 0) {
            idvMultiplier = BigDecimal.valueOf(1.1); // +10% for medium-value
        } else if (idv.compareTo(BigDecimal.valueOf(200000)) < 0) {
            idvMultiplier = BigDecimal.valueOf(0.9); // -10% discount for low-value
        }

        BigDecimal finalTotalMultiplier = riskMultiplier.multiply(idvMultiplier);

        return applicablePlans.stream().map(plan -> {
            QuoteResponseDTO dto = new QuoteResponseDTO();
            dto.setPlanId(plan.getPlanId());
            dto.setPlanName(plan.getPlanName());
            dto.setPolicyType(plan.getPolicyType());
            dto.setDescription(plan.getDescription());
            dto.setBasePremium(plan.getBasePremium());
            
            List<QuoteOptionDTO> generatedOptions = new ArrayList<>();
            
            // 1 Year Plan (1.0x baseline)
            QuoteOptionDTO opt1 = new QuoteOptionDTO();
            opt1.setTenureYears(1);
            opt1.setCalculatedPremium(plan.getBasePremium().multiply(finalTotalMultiplier).setScale(2, RoundingMode.HALF_UP));
            generatedOptions.add(opt1);

            // 2 Year Plan (1.9x baseline - multi-year discount)
            QuoteOptionDTO opt2 = new QuoteOptionDTO();
            opt2.setTenureYears(2);
            opt2.setCalculatedPremium(plan.getBasePremium().multiply(finalTotalMultiplier).multiply(BigDecimal.valueOf(1.9)).setScale(2, RoundingMode.HALF_UP));
            generatedOptions.add(opt2);

            // 3 Year Plan (2.8x baseline - multi-year discount)
            QuoteOptionDTO opt3 = new QuoteOptionDTO();
            opt3.setTenureYears(3);
            opt3.setCalculatedPremium(plan.getBasePremium().multiply(finalTotalMultiplier).multiply(BigDecimal.valueOf(2.8)).setScale(2, RoundingMode.HALF_UP));
            generatedOptions.add(opt3);
            
            dto.setOptions(generatedOptions);
            dto.setIdv(idv.setScale(2, RoundingMode.HALF_UP));
            dto.setRiskLevel(risk);
            return dto;
        }).collect(Collectors.toList());
    }
}
