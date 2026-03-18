package org.thehartford.willowshield.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResDTO {
    private String token;
    private String username;
    private String role;
}
