package com.ecommerce.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
    @NotBlank ( message = "Email is Required")
    @Email (message = "Enter a valid email address")
    String email,

    @NotBlank (message = "Password is Required")
    String password
){
    
}
