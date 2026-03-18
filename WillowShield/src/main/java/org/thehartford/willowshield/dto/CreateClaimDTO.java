package org.thehartford.willowshield.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.thehartford.willowshield.enums.ClaimType;

@Data
public class CreateClaimDTO {

    @NotNull(message = "Policy ID is required")
    private Integer policyId;

    @NotNull(message = "Claim Type is required")
    private ClaimType claimType;

    private String description;

    public Integer getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Integer policyId) {
        this.policyId = policyId;
    }

    public ClaimType getClaimType() {
        return claimType;
    }

    public void setClaimType(ClaimType claimType) {
        this.claimType = claimType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String bankAccountNumber;
    private String ifscCode;
    private String accountHolderName;
}
