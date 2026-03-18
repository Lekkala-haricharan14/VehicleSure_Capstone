package org.thehartford.willowshield.dto;

import lombok.Data;
import org.thehartford.willowshield.enums.UserRole;

import java.time.LocalDateTime;

@Data
public class ReadStaffDTO {

    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String fullName;
    private UserRole role;
    private boolean isActive;
    private LocalDateTime createdAt;

}
