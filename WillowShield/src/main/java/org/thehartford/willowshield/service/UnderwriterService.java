package org.thehartford.willowshield.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thehartford.willowshield.dto.ReadVehicleApplicationDTO;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.entity.Vehicle;
import org.thehartford.willowshield.entity.VehicleApplication;
import org.thehartford.willowshield.entity.VehicleDocument;
import org.thehartford.willowshield.enums.VehicleApplicationStatus;
import org.thehartford.willowshield.repository.UserRepository;
import org.thehartford.willowshield.repository.VehicleApplicationRepository;
import org.thehartford.willowshield.repository.VehicleRepository;
import org.thehartford.willowshield.repository.PolicyRepository;
import org.thehartford.willowshield.entity.Policy;
import org.thehartford.willowshield.enums.PolicyStatus;
import org.thehartford.willowshield.exceptions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class UnderwriterService {

    @Autowired
    private VehicleApplicationRepository vehicleApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private NotificationService notificationService;

    public List<ReadVehicleApplicationDTO> getAssignedApplications(Long underwriterId) {
        MyUser underwriter = userRepository.findById(underwriterId)
                .orElseThrow(() -> new UserNotFoundException(underwriterId));

        List<VehicleApplication> applications = vehicleApplicationRepository.findAll().stream()
                .filter(app -> app.getAssignedUnderwriter() != null
                        && app.getAssignedUnderwriter().getId().equals(underwriter.getId()))
                .toList();

        return applications.stream().map(this::mapToApplicationDTO).toList();
    }

    public void approveApplication(Integer appId, Long underwriterId) {
        VehicleApplication app = getAssignedApplication(appId, underwriterId);

        if (app.getStatus() != VehicleApplicationStatus.ASSIGNED
                && app.getStatus() != VehicleApplicationStatus.UNDER_REVIEW) {
            throw new InvalidStateException("Can only approve ASSIGNED or UNDER_REVIEW applications. Current status: " + app.getStatus());
        }

        // Create the Vehicle for the user
        Vehicle vehicle = new Vehicle();
        vehicle.setRegistrationNumber(app.getRegistrationNumber());
        vehicle.setMake(app.getMake());
        vehicle.setModel(app.getModel());
        vehicle.setYear(app.getYear());
        vehicle.setFuelType(app.getFuelType());
        vehicle.setVehicleType(app.getVehicleType());
        vehicle.setTransmissionType(app.getTransmissionType());
        vehicle.setAccidentsInPast(app.getAccidentsInPast());
        vehicle.setRiskLevel(app.getRiskLevel());
        vehicle.setCustomer(app.getCustomer());
        vehicle.setVehicleApplication(app);

        vehicleRepository.save(vehicle);

        // Create the Policy entry
        Policy policy = new Policy();
        policy.setPolicyNumber("POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        policy.setStatus(PolicyStatus.PENDING_PAYMENT);
        policy.setStartDate(LocalDate.now());
        policy.setEndDate(LocalDate.now().plusYears(app.getTenureYears()));
        policy.setPremiumAmount(app.getCalculatedPremium());
        policy.setCustomer(app.getCustomer());
        policy.setUnderwriter(app.getAssignedUnderwriter());
        policy.setVehicle(vehicle);
        policy.setPlan(app.getPlan());

        policyRepository.save(policy);

        app.setStatus(VehicleApplicationStatus.APPROVED);
        app.setRejectionReason(null);
        vehicleApplicationRepository.save(app);

        notificationService.createNotification(app.getCustomer(), 
            "Your policy application for " + app.getRegistrationNumber() + " has been approved. Please proceed with the premium payment.", 
            "POLICY_APPROVED");
    }

    public void rejectApplication(Integer appId, Long underwriterId, String reason) {
        VehicleApplication app = getAssignedApplication(appId, underwriterId);

        if (app.getStatus() != VehicleApplicationStatus.ASSIGNED
                && app.getStatus() != VehicleApplicationStatus.UNDER_REVIEW) {
            throw new InvalidStateException("Can only reject ASSIGNED or UNDER_REVIEW applications. Current status: " + app.getStatus());
        }

        app.setStatus(VehicleApplicationStatus.REJECTED);
        app.setRejectionReason(reason);
        vehicleApplicationRepository.save(app);

        notificationService.createNotification(app.getCustomer(), 
            "Your policy application for " + app.getRegistrationNumber() + " has been rejected. Reason: " + reason, 
            "POLICY_REJECTED");
    }

    private VehicleApplication getAssignedApplication(Integer appId, Long underwriterId) {
        MyUser underwriter = userRepository.findById(underwriterId)
                .orElseThrow(() -> new UserNotFoundException(underwriterId));

        VehicleApplication app = vehicleApplicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationNotFoundException(appId));

        if (app.getAssignedUnderwriter() == null || !app.getAssignedUnderwriter().getId().equals(underwriter.getId())) {
            throw new UnauthorizedAccessException("Application is not assigned to you");
        }

        return app;
    }

    private ReadVehicleApplicationDTO mapToApplicationDTO(VehicleApplication app) {
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

        if (app.getDocuments() != null && !app.getDocuments().isEmpty()) {
            VehicleDocument doc = app.getDocuments().get(0);
            dto.setRcDocumentPath(doc.getRcDocumentPath());
            dto.setInvoiceDocumentPath(doc.getInvoiceDocumentPath());
        }

        return dto;
    }
}
