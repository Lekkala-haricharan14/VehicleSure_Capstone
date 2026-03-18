package org.thehartford.willowshield.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data

public class RegisterReqDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Phone number must contain 10–15 digits and may include country code"
    )
    private String phoneNumber;
}
