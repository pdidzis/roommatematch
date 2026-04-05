package com.roommatematch.service;

import com.roommatematch.dto.request.LoginRequest;
import com.roommatematch.dto.request.RegisterRequest;
import com.roommatematch.dto.response.AuthResponse;
import com.roommatematch.exception.DuplicateResourceException;
import com.roommatematch.model.entity.User;
import com.roommatematch.model.entity.UserPreferences;
import com.roommatematch.model.enums.UserRole;
import com.roommatematch.repository.UserPreferencesRepository;
import com.roommatematch.repository.UserRepository;
import com.roommatematch.security.CustomUserDetailsService;
import com.roommatematch.security.JwtUtil;
import com.roommatematch.util.InputSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPreferencesRepository userPreferencesRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private InputSanitizer sanitizer;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .password("password123")
                .role(UserRole.TENANT)
                .phoneNumber("+1234567890")
                .build();

        loginRequest = LoginRequest.builder()
                .email("john@test.com")
                .password("password123")
                .build();

        savedUser = User.builder()
                .id(1L)
                .email("john@test.com")
                .passwordHash("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.TENANT)
                .isActive(true)
                .isVerified(false)
                .build();

        mockUserDetails = new org.springframework.security.core.userdetails.User(
                "john@test.com",
                "hashedPassword",
                Collections.emptyList()
        );
    }

    @Test
    @DisplayName("register saves user and creates preferences")
    void testRegisterSavesUserAndPreferences() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(sanitizer.sanitize(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sanitizer.sanitizeOrNull(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userPreferencesRepository.save(any(UserPreferences.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(customUserDetailsService.loadUserByUsername("john@test.com")).thenReturn(mockUserDetails);
        when(jwtUtil.generateToken(mockUserDetails)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(registerRequest);

        verify(userRepository, times(1)).save(any(User.class));
        verify(userPreferencesRepository, times(1)).save(any(UserPreferences.class));
        assertEquals("mock.jwt.token", response.getToken());
    }

    @Test
    @DisplayName("register throws exception for duplicate email")
    void testRegisterDuplicateEmailThrows() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(savedUser));

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
    }

    @Test
    @DisplayName("login returns valid auth response")
    void testLoginReturnsAuthResponse() {
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(savedUser));
        when(customUserDetailsService.loadUserByUsername("john@test.com")).thenReturn(mockUserDetails);
        when(jwtUtil.generateToken(mockUserDetails)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.login(loginRequest);

        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("john@test.com", response.getEmail());
    }

    @Test
    @DisplayName("login throws exception for invalid credentials")
    void testLoginInvalidCredentialsThrows() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
