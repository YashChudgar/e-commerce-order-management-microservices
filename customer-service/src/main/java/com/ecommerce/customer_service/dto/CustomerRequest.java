package com.ecommerce.customer_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerRequest {

    @NotBlank(message = "Name is Required")
    @Size(max=100, message = "Name cannot exceed 100 characters")
    private String name;

    
    @NotBlank(message = "Email is Required")
    @Email(message = "Email must be valid")
    @Size(max=150, message="Email cannot exceed 150 characters")
    private String email;
    
    
    public CustomerRequest(){
        
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
