package com.roommatematch.service;

import com.roommatematch.dto.request.PreferencesRequest;
import com.roommatematch.dto.request.UpdateProfileRequest;
import com.roommatematch.dto.response.PreferencesResponse;
import com.roommatematch.dto.response.UserProfileResponse;
import com.roommatematch.exception.BusinessLogicException;
import com.roommatematch.exception.ResourceNotFoundException;
import com.roommatematch.model.entity.User;
import com.roommatematch.model.entity.UserPreferences;
import com.roommatematch.repository.UserPreferencesRepository;
import com.roommatematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferencesRepository userPreferencesRepository;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return mapToUserProfileResponse(user);
    }

    public UserProfileResponse getProfileByEmail(String email) {
        User user = getCurrentUser(email);
        return mapToUserProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());

        user = userRepository.save(user);
        log.info("Profile updated for user: {}", userId);

        return mapToUserProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updatePreferences(Long userId, PreferencesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        UserPreferences preferences = user.getPreferences();
        if (preferences == null) {
            preferences = new UserPreferences();
            preferences.setUser(user);
        }

        preferences.setMinBudget(request.getMinBudget());
        preferences.setMaxBudget(request.getMaxBudget());
        preferences.setCity(request.getCity());
        preferences.setMoveInDate(request.getMoveInDate());
        preferences.setPetsAllowed(request.isPetsAllowed());
        preferences.setSmokingAllowed(request.isSmokingAllowed());
        preferences.setGenderPreference(request.getGenderPreference());
        preferences.setSleepSchedule(request.getSleepSchedule());
        preferences.setCleanlinessLevel(request.getCleanlinessLevel());
        preferences.setSocialHabits(request.getSocialHabits());
        preferences.setWorkFromHome(request.getWorkFromHome());
        preferences.setGuestFrequency(request.getGuestFrequency());
        preferences.setNoiseLevel(request.getNoiseLevel());

        userPreferencesRepository.save(preferences);
        log.info("Preferences updated for user: {}", userId);

        // Reload user to get updated preferences
        user = userRepository.findById(userId).orElseThrow();
        return mapToUserProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse uploadProfilePhoto(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (file.isEmpty()) {
            throw new BusinessLogicException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessLogicException("Invalid file type. Only jpg, png, gif, webp allowed");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessLogicException("File size exceeds 5MB limit");
        }

        try {
            Path uploadDir = Paths.get("./uploads/profiles/" + userId);
            Files.createDirectories(uploadDir);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = userId + "_" + System.currentTimeMillis() + extension;

            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            user.setProfilePhotoUrl(filePath.toString());
            userRepository.save(user);

            log.info("Profile photo uploaded for user: {}", userId);

            return mapToUserProfileResponse(user);
        } catch (IOException e) {
            log.error("Failed to upload profile photo for user: {}", userId, e);
            throw new BusinessLogicException("Failed to upload profile photo");
        }
    }

    private UserProfileResponse mapToUserProfileResponse(User user) {
        PreferencesResponse preferencesResponse = null;
        if (user.getPreferences() != null) {
            preferencesResponse = mapToPreferencesResponse(user.getPreferences());
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .role(user.getRole())
                .isVerified(user.isVerified())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .preferences(preferencesResponse)
                .build();
    }

    private PreferencesResponse mapToPreferencesResponse(UserPreferences prefs) {
        return PreferencesResponse.builder()
                .id(prefs.getId())
                .minBudget(prefs.getMinBudget())
                .maxBudget(prefs.getMaxBudget())
                .city(prefs.getCity())
                .moveInDate(prefs.getMoveInDate())
                .petsAllowed(prefs.isPetsAllowed())
                .smokingAllowed(prefs.isSmokingAllowed())
                .genderPreference(prefs.getGenderPreference())
                .sleepSchedule(prefs.getSleepSchedule())
                .cleanlinessLevel(prefs.getCleanlinessLevel())
                .socialHabits(prefs.getSocialHabits())
                .workFromHome(prefs.getWorkFromHome())
                .guestFrequency(prefs.getGuestFrequency())
                .noiseLevel(prefs.getNoiseLevel())
                .build();
    }
}
