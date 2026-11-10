package com.ecommerce.customer_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ecommerce.customer_service.entity.Customer;
import com.ecommerce.customer_service.exceptions.EmailAlreadyExistsException;
import com.ecommerce.customer_service.exceptions.ResourceNotFoundException;
import com.ecommerce.customer_service.repository.CustomerRepository;

import jakarta.transaction.Transactional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(Customer customer) {
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "A customer with this email already exists");
        }
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + id));
    }

    @Transactional
    public Customer updateCustomer(long id, Customer updatedCustomer) {
        Customer existingCustomer = getCustomerById(id);

        customerRepository.findByEmail(updatedCustomer.getEmail())
                .filter(customer -> !customer.getId().equals(id))
                .ifPresent(customer -> {
                    throw new EmailAlreadyExistsException(
                            "A Customer with this email already exists");
                });
        existingCustomer.setName(updatedCustomer.getName());
        existingCustomer.setEmail(updatedCustomer.getEmail());
        return customerRepository.save(existingCustomer);
    }

    public void deleteCustomer(Long id){
        Customer customer = getCustomerById(id);
        customerRepository.delete(customer);
    }  

    public List<Customer> searchCustomers(String name, String email){
        if(email != null && !email.isBlank()){
            return customerRepository.findByEmail(email)
                .map(List::of)
                .orElseGet(List::of);
        }
        
        if (name != null && !name.isBlank()){
            return customerRepository.findByNameContainingIgnoreCase(name);
        }

        return customerRepository.findAll();
    }
}
