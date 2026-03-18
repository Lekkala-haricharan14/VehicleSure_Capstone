package org.thehartford.willowshield.dto;

import lombok.Data;
import org.thehartford.willowshield.enums.VehicleApplicationStatus;
import org.thehartford.willowshield.enums.VehicleType;
import org.thehartford.willowshield.enums.TransmissionType;
import org.thehartford.willowshield.enums.RiskLevel;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class ReadVehicleApplicationDTO {

    private Integer vehicleApplicationId;
    private Integer policyId;
    private String vehicleOwnerName;
    private String registrationNumber;
    private String make;
    private String model;
    private Integer year;
    private String fuelType;
    private String chassisNumber;
    private Long distanceDriven;

    private BigDecimal exShowroomPrice;
    private BigDecimal idv;
    private BigDecimal calculatedPremium;
    private Integer tenureYears;

    private VehicleType vehicleType;
    private TransmissionType transmissionType;
    private Integer accidentsInPast;
    private RiskLevel riskLevel;

    private VehicleApplicationStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;

    // Details of the plan associated with the application
    private Integer planId;
    private String planName;
    private String policyType;
    private String description;
    private BigDecimal basePremium;

    private String customerName;
    private String customerEmail;

    // Assigned Underwriter
    private Long assignedUnderwriterId;
    private String assignedUnderwriterName;

    // Document Paths
    private String rcDocumentPath;

    private String invoiceDocumentPath;
    private String generatedInvoicePath;
    private String generatedPolicyPath;

    public String getRcDocumentPath() {
        return rcDocumentPath;
    }

    public void setRcDocumentPath(String rcDocumentPath) {
        this.rcDocumentPath = rcDocumentPath;
    }

    public String getInvoiceDocumentPath() {
        return invoiceDocumentPath;
    }

    public void setInvoiceDocumentPath(String invoiceDocumentPath) {
        this.invoiceDocumentPath = invoiceDocumentPath;
    }

    public String getGeneratedInvoicePath() {
        return generatedInvoicePath;
    }

    public void setGeneratedInvoicePath(String generatedInvoicePath) {
        this.generatedInvoicePath = generatedInvoicePath;
    }

    public String getGeneratedPolicyPath() {
        return generatedPolicyPath;
    }

    public void setGeneratedPolicyPath(String generatedPolicyPath) {
        this.generatedPolicyPath = generatedPolicyPath;
    }
}
