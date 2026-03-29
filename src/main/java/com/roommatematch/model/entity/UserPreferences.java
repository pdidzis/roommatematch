package com.roommatematch.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_preferences")
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

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
