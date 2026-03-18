package org.thehartford.willowshield.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnderwriterWorkloadDTO {
    private Long id;
    private String username;
    private String email;
    private Long activeApplicationsCount;
}
