package com.roommatematch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummary {

    private Long userId;
    private String firstName;
    private String lastName;
    private String profilePhotoUrl;
    private String city;
}
