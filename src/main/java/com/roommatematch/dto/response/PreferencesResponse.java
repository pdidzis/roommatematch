package com.roommatematch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferencesResponse {

    private Long id;
    private Integer minBudget;
    private Integer maxBudget;
    private String city;
    private LocalDate moveInDate;
    private boolean petsAllowed;
    private boolean smokingAllowed;
    private String genderPreference;
    private Integer sleepSchedule;
    private Integer cleanlinessLevel;
    private Integer socialHabits;
    private Integer workFromHome;
    private Integer guestFrequency;
    private Integer noiseLevel;
}
