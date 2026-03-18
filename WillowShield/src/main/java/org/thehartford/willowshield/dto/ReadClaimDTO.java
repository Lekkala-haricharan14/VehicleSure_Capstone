package org.thehartford.willowshield.dto;

import lombok.Data;
import org.thehartford.willowshield.enums.ClaimStatus;
import org.thehartford.willowshield.enums.ClaimType;
import java.math.BigDecimal;

@Data
public class ReadClaimDTO {
    private Integer claimId;
    private String claimNumber;
    private ClaimType claimType;
    private ClaimStatus status;
    private BigDecimal approvedAmount;
    private Integer policyId;
    private String policyNumber;
    private String customerName;
    private String document1Path;
    private String document2Path;
    private String document3Path;

    private Long claimsOfficerId;
    private String claimsOfficerName;

    private String rejectionReason;

    // Getters and Setters
    public Integer getClaimId() {
        return claimId;
    }

    public void setClaimId(Integer claimId) {
        this.claimId = claimId;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public ClaimType getClaimType() {
        return claimType;
    }

    public void setClaimType(ClaimType claimType) {
        this.claimType = claimType;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public Integer getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Integer policyId) {
        this.policyId = policyId;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getDocument1Path() {
        return document1Path;
    }

    public void setDocument1Path(String document1Path) {
        this.document1Path = document1Path;
    }

    public String getDocument2Path() {
        return document2Path;
    }

    public void setDocument2Path(String document2Path) {
        this.document2Path = document2Path;
    }

    public String getDocument3Path() {
        return document3Path;
    }

    public void setDocument3Path(String document3Path) {
        this.document3Path = document3Path;
    }

    public Long getClaimsOfficerId() {
        return claimsOfficerId;
    }

    public void setClaimsOfficerId(Long claimsOfficerId) {
        this.claimsOfficerId = claimsOfficerId;
    }

    public String getClaimsOfficerName() {
        return claimsOfficerName;
    }

    public void setClaimsOfficerName(String claimsOfficerName) {
        this.claimsOfficerName = claimsOfficerName;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    private String bankAccountNumber;
    private String ifscCode;
    private String accountHolderName;
}
