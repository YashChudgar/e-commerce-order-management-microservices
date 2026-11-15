package com.ecommerce.customer_service.service;

import com.ecommerce.customer_service.entity.Customer;
import com.ecommerce.customer_service.exceptions.EmailAlreadyExistsException;
import com.ecommerce.customer_service.exceptions.ResourceNotFoundException;
import com.ecommerce.customer_service.repository.CustomerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer(
            "John Smith",
            "john@example.com"
        );

        ReflectionTestUtils.setField(customer, "id", 1L);
    }

    // 1. Create customer successfully
    @Test
    void createCustomerSuccessfully() {

        when(customerRepository.existsByEmail(customer.getEmail()))
            .thenReturn(false);

        when(customerRepository.save(customer))
            .thenReturn(customer);

        Customer result = customerService.createCustomer(customer);

        assertNotNull(result);
        assertEquals("John Smith", result.getName());
        assertEquals("john@example.com", result.getEmail());

        verify(customerRepository).save(customer);
    }

    // 2. Reject duplicate email
    @Test
    void createCustomerWithDuplicateEmail() {

        when(customerRepository.existsByEmail(customer.getEmail()))
            .thenReturn(true);

        assertThrows(
            EmailAlreadyExistsException.class,
            () -> customerService.createCustomer(customer)
        );

        verify(customerRepository, never()).save(any(Customer.class));
    }

    // 3. Retrieve all customers
    @Test
    void getAllCustomersSuccessfully() {

        List<Customer> customers = List.of(customer);

        when(customerRepository.findAll())
            .thenReturn(customers);

        List<Customer> result = customerService.getAllCustomers();

        assertEquals(1, result.size());
        assertEquals("John Smith", result.get(0).getName());
    }

    // 4. Retrieve customer by ID
    @Test
    void getCustomerByIdSuccessfully() {

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomerById(1L);

        assertNotNull(result);
        assertEquals("John Smith", result.getName());
    }

    // 5. Customer not found
    @Test
    void getCustomerByIdNotFound() {

        when(customerRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> customerService.getCustomerById(99L)
        );
    }

    // 6. Update customer successfully
    @Test
    void updateCustomerSuccessfully() {

        Customer updatedCustomer = new Customer(
            "John Updated",
            "john.updated@example.com"
        );

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(customer));

        when(customerRepository.findByEmail(updatedCustomer.getEmail()))
            .thenReturn(Optional.empty());

        when(customerRepository.save(any(Customer.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = customerService.updateCustomer(1L, updatedCustomer);

        assertEquals("John Updated", result.getName());
        assertEquals("john.updated@example.com", result.getEmail());

        verify(customerRepository).save(customer);
    }

    // 7. Reject duplicate email during update
    @Test
    void updateCustomerWithDuplicateEmail() {

        Customer anotherCustomer = new Customer(
            "Another Customer",
            "another@example.com"
        );

        ReflectionTestUtils.setField(anotherCustomer, "id", 2L);

        Customer updatedCustomer = new Customer(
            "John Updated",
            "another@example.com"
        );

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(customer));

        when(customerRepository.findByEmail("another@example.com"))
            .thenReturn(Optional.of(anotherCustomer));

        assertThrows(
            EmailAlreadyExistsException.class,
            () -> customerService.updateCustomer(1L, updatedCustomer)
        );

        verify(customerRepository, never()).save(any(Customer.class));
    }

    // 8. Delete customer successfully
    @Test
    void deleteCustomerSuccessfully() {

        when(customerRepository.findById(1L))
            .thenReturn(Optional.of(customer));

        customerService.deleteCustomer(1L);

        verify(customerRepository).delete(customer);
    }

    // 9. Delete customer that does not exist
    @Test
    void deleteCustomerNotFound() {

        when(customerRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> customerService.deleteCustomer(99L)
        );

        verify(customerRepository, never()).delete(any(Customer.class));
    }

    // 10. Search customers by email
    @Test
    void searchCustomersByEmail() {

        when(customerRepository.findByEmail("john@example.com"))
            .thenReturn(Optional.of(customer));

        List<Customer> result = customerService.searchCustomers(
            null,
            "john@example.com"
        );

        assertEquals(1, result.size());
        assertEquals("john@example.com", result.get(0).getEmail());
    }

    // 11. Search customers by name
    @Test
    void searchCustomersByName() {

        when(customerRepository.findByNameContainingIgnoreCase("john"))
            .thenReturn(List.of(customer));

        List<Customer> result = customerService.searchCustomers(
            "john",
            null
        );

        assertEquals(1, result.size());
        assertEquals("John Smith", result.get(0).getName());
    }

    // 12. Retrieve all customers when no filters are supplied
    @Test
    void searchCustomersWithoutFilters() {

        when(customerRepository.findAll())
            .thenReturn(List.of(customer));

        List<Customer> result = customerService.searchCustomers(
            null,
            null
        );

        assertEquals(1, result.size());

        verify(customerRepository).findAll();
    }
}

