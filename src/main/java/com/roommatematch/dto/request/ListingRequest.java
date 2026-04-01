package com.roommatematch.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Country is required")
    private String country;

    @NotNull(message = "Monthly rent is required")
    @DecimalMin(value = "0.0", message = "Monthly rent must be at least 0")
    private BigDecimal monthlyRent;

    private LocalDate availableFrom;

    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms;

    @Min(value = 1, message = "Available rooms must be at least 1")
    private Integer availableRooms;

    private boolean petsAllowed;

    private boolean smokingAllowed;
}
