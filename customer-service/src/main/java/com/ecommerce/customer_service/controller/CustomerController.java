package com.ecommerce.customer_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.customer_service.dto.CustomerRequest;
import com.ecommerce.customer_service.entity.Customer;
import com.ecommerce.customer_service.exceptions.ResourceNotFoundException;
import com.ecommerce.customer_service.service.CustomerService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;


@RestController 
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService){
        this.customerService = customerService;
    }

    @PostMapping 
    @ResponseStatus (HttpStatus.CREATED)
    public Customer createCustomer(@Valid @RequestBody CustomerRequest request) {
        Customer customer = new Customer(
            request.getName(),
            request.getEmail()
        );

        return customerService.createCustomer(customer);
    }

    @GetMapping 
    public List<Customer> getAllCustomers(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String email
    ) {
        return customerService.searchCustomers(name,email);
    }
    
    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @PutMapping("/{id}")
    public Customer updatedCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        Customer updatedCustomer = new Customer(
            request.getName(),
            request.getEmail()
        );

        return customerService.updateCustomer(id, updatedCustomer);   
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable Long id){
        customerService.deleteCustomer(id);
    }
    
}
