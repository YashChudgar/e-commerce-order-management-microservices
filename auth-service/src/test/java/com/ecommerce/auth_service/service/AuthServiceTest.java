package com.ecommerce.auth_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.ecommerce.auth_service.dto.AuthResponse;
import com.ecommerce.auth_service.dto.LoginRequest;
import com.ecommerce.auth_service.dto.RegisterRequest;
import com.ecommerce.auth_service.entity.User;
import com.ecommerce.auth_service.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole("USER");
    }

    // 1. Register successfully
    @Test
    void registerSuccessfully() {

        RegisterRequest request =
                new RegisterRequest("test@example.com", "password123");

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        when(jwtService.generateToken(
                1L, "test@example.com", "USER"))
                .thenReturn("test-jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("test-jwt-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(1L, response.userId());
        assertEquals("test@example.com", response.email());
        assertEquals("USER", response.role());

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
        verify(jwtService).generateToken(
                1L, "test@example.com", "USER");
    }

    // 2. Register with existing email
    @Test
    void registerWithDuplicateEmail() {

        RegisterRequest request =
                new RegisterRequest("test@example.com", "password123");

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(jwtService, never()).generateToken(
                anyLong(), anyString(), anyString());
    }

    // 3. Register with uppercase and spaced email
    @Test
    void registerWithUppercaseEmail() {

        RegisterRequest request =
                new RegisterRequest(" TEST@EXAMPLE.COM ", "password123");

        when(userRepository.existsByEmail(" TEST@EXAMPLE.COM "))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User savedUser = invocation.getArgument(0);
                    savedUser.setId(1L);
                    return savedUser;
                });

        when(jwtService.generateToken(
                1L, "test@example.com", "USER"))
                .thenReturn("test-jwt-token");

        AuthResponse response = authService.register(request);

        assertEquals("test@example.com", response.email());

        verify(userRepository).save(argThat(savedUser ->
                savedUser.getEmail().equals("test@example.com")
        ));
    }

    // 4. Verify password is encrypted before saving
    @Test
    void registerEncryptsPassword() {

        RegisterRequest request =
                new RegisterRequest("test@example.com", "password123");

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User savedUser = invocation.getArgument(0);
                    savedUser.setId(1L);
                    return savedUser;
                });

        when(jwtService.generateToken(
                anyLong(), anyString(), anyString()))
                .thenReturn("test-jwt-token");

        authService.register(request);

        verify(passwordEncoder).encode("password123");

        verify(userRepository).save(argThat(savedUser ->
                savedUser.getPassword().equals("hashed-password")
        ));
    }

    // 5. Login successfully
    @Test
    void loginSuccessfully() {

        LoginRequest request =
                new LoginRequest("test@example.com", "password123");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123", "encodedPassword"))
                .thenReturn(true);

        when(jwtService.generateToken(
                1L, "test@example.com", "USER"))
                .thenReturn("test-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("test-jwt-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(1L, response.userId());
        assertEquals("test@example.com", response.email());
        assertEquals("USER", response.role());

        verify(passwordEncoder).matches(
                "password123", "encodedPassword");

        verify(jwtService).generateToken(
                1L, "test@example.com", "USER");
    }

    // 6. Login with incorrect password
    @Test
    void loginWithIncorrectPassword() {

        LoginRequest request =
                new LoginRequest("test@example.com", "wrongPassword");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrongPassword", "encodedPassword"))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verify(jwtService, never()).generateToken(
                anyLong(), anyString(), anyString());
    }

    // 7. Login with email that does not exist
    @Test
    void loginWithNonExistingEmail() {

        LoginRequest request =
                new LoginRequest("unknown@example.com", "password123");

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verify(passwordEncoder, never()).matches(
                anyString(), anyString());

        verify(jwtService, never()).generateToken(
                anyLong(), anyString(), anyString());
    }

    // 8. Login with uppercase and spaced email
    @Test
    void loginWithUppercaseEmail() {

        LoginRequest request =
                new LoginRequest(" TEST@EXAMPLE.COM ", "password123");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123", "encodedPassword"))
                .thenReturn(true);

        when(jwtService.generateToken(
                1L, "test@example.com", "USER"))
                .thenReturn("test-jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("test@example.com", response.email());

        verify(userRepository).findByEmail("test@example.com");
    }
}
