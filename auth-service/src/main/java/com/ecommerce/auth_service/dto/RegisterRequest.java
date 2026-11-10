package com.ecommerce.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest (
    @NotBlank(message = "Email is Required")
    @Email (message = "Enter a valid email address")
    String email,

    @NotBlank(message = "Password is Required")
    @Size(min = 8, message = "Password must be atleast 8 characters long")
    String password
    
) {}
