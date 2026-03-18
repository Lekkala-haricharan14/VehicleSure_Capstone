package org.thehartford.willowshield.dto;

import lombok.Data;
import org.thehartford.willowshield.enums.VehicleApplicationStatus;

@Data
public class UpdateApplicationStatusDTO {
    private VehicleApplicationStatus status;
    private String rejectionReason;
}
