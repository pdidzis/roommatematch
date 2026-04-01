package com.roommatematch.controller;

import com.roommatematch.dto.request.PreferencesRequest;
import com.roommatematch.dto.request.UpdateProfileRequest;
import com.roommatematch.dto.response.UserProfileResponse;
import com.roommatematch.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management")
public class UserController {

    private final UserService userService;

    private Long getCurrentUserId(Authentication authentication) {
        String email = authentication.getName();
        UserProfileResponse profile = userService.getProfileByEmail(email);
        return profile.getId();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    @ApiResponse(responseCode = "200", description = "Profile retrieved")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<UserProfileResponse> getCurrentProfile(Authentication authentication) {
        String email = authentication.getName();
        UserProfileResponse profile = userService.getProfileByEmail(email);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        UserProfileResponse profile = userService.getProfileByEmail(email);
        UserProfileResponse updatedProfile = userService.updateProfile(profile.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/me/preferences")
    @Operation(summary = "Update lifestyle preferences")
    @ApiResponse(responseCode = "200", description = "Preferences updated")
    public ResponseEntity<UserProfileResponse> updatePreferences(
            @Valid @RequestBody PreferencesRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        UserProfileResponse profile = userService.getProfileByEmail(email);
        UserProfileResponse updatedProfile = userService.updatePreferences(profile.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile photo")
    @ApiResponse(responseCode = "200", description = "Photo uploaded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid file type or size")
    public ResponseEntity<UserProfileResponse> uploadProfilePhoto(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String email = authentication.getName();
        UserProfileResponse profile = userService.getProfileByEmail(email);
        UserProfileResponse updatedProfile = userService.uploadProfilePhoto(profile.getId(), file);
        return ResponseEntity.ok(updatedProfile);
    }
}
