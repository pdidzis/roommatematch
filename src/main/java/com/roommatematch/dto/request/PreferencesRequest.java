package com.roommatematch.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferencesRequest {

    private Integer minBudget;

    private Integer maxBudget;

    private String city;

    private LocalDate moveInDate;

    private boolean petsAllowed;

    private boolean smokingAllowed;

    private String genderPreference;

    @Min(1)
    @Max(5)
    private Integer sleepSchedule;

    @Min(1)
    @Max(5)
    private Integer cleanlinessLevel;

    @Min(1)
    @Max(5)
    private Integer socialHabits;

    @Min(1)
    @Max(5)
    private Integer workFromHome;

    @Min(1)
    @Max(5)
    private Integer guestFrequency;

    @Min(1)
    @Max(5)
    private Integer noiseLevel;
}
