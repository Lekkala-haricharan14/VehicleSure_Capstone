package org.thehartford.willowshield.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

/**
 * JWT Login Request DTO
 *
 * Request Body Example:
 * {
 *   "email": "haricharan@example.com",
 *   "password": "password123"
 * }
 *
 * Note: User can login using EMAIL instead of username
 * Email is unique and more user-friendly
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
