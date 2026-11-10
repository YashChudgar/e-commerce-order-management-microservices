package com.ecommerce.auth_service.dto;

public record AuthResponse(
    String token,
    String tokenType,
    Long userId,
    String email,
    String role
) {
    
}
