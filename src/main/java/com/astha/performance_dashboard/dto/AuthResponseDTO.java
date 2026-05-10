package com.astha.performance_dashboard.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AuthResponseDTO {
    private String token;
    private String refreshToken;
    private String username;
    private Set<String> roles;
    private Long expiresIn;
}
