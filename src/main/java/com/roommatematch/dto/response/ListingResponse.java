package com.roommatematch.dto.response;

import com.roommatematch.model.enums.ListingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingResponse {

    private Long id;

    private String title;

    private String description;

    private String address;

    private String city;

    private String country;

    private BigDecimal monthlyRent;

    private LocalDate availableFrom;

    private Integer totalRooms;

    private Integer availableRooms;

    private boolean petsAllowed;

    private boolean smokingAllowed;

    private List<String> photoUrls;

    private ListingStatus status;

    private boolean isVerified;

    private String landlordName;

    private String landlordEmail;

    private String landlordPhone;

    private String createdAt;

    private Long daysUntilAvailable;

    @Builder.Default
    private boolean currentUserInterested = false;

    @Builder.Default
    private boolean bothRoommatesInterested = false;

    private Long landlordChatId;
}
