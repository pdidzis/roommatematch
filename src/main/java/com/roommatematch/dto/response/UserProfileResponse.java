package com.roommatematch.dto.response;

import com.roommatematch.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profilePhotoUrl;
    private UserRole role;
    private boolean isVerified;
    private boolean isActive;
    private String createdAt;
    private PreferencesResponse preferences;
}
