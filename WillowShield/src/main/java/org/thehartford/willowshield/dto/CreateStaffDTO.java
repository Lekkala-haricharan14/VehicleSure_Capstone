package org.thehartford.willowshield.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.thehartford.willowshield.enums.UserRole;

@Data
public class CreateStaffDTO {

    @NotBlank(message = "Username is required")
    private String username;
    
    private String password;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Role is required")
    private UserRole role; // UNDERWRITER or CLAIMSOFFICER
}
