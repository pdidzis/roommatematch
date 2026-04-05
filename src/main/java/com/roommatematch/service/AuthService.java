package com.roommatematch.service;

import com.roommatematch.dto.request.LoginRequest;
import com.roommatematch.dto.request.RegisterRequest;
import com.roommatematch.dto.response.AuthResponse;
import com.roommatematch.exception.DuplicateResourceException;
import com.roommatematch.exception.ResourceNotFoundException;
import com.roommatematch.model.entity.User;
import com.roommatematch.model.entity.UserPreferences;
import com.roommatematch.repository.UserPreferencesRepository;
import com.roommatematch.repository.UserRepository;
import com.roommatematch.security.CustomUserDetailsService;
import com.roommatematch.security.JwtUtil;
import com.roommatematch.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final NotificationService notificationService;
    private final InputSanitizer sanitizer;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(sanitizer.sanitize(request.getFirstName()))
                .lastName(sanitizer.sanitize(request.getLastName()))
                .phoneNumber(sanitizer.sanitizeOrNull(request.getPhoneNumber()))
                .role(request.getRole())
                .isVerified(false)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        UserPreferences preferences = UserPreferences.builder()
                .user(user)
                .build();
        userPreferencesRepository.save(preferences);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        log.info("New user registered: {}", user.getEmail());

        notificationService.createNotification(
                user.getId(),
                "Welcome to RoommateMatch! 🎉",
                "Your account has been created. Complete your preferences to start finding roommates.",
                "WELCOME",
                null
        );

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .build();
    }
}
