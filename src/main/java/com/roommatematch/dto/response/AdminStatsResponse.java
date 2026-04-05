package com.roommatematch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {

    private Long totalUsers;

    private Map<String, Long> usersByRole;

    private Long totalListings;

    private Map<String, Long> listingsByStatus;

    private Long totalMatches;

    private Map<String, Long> matchesByStatus;

    private Long totalMessages;

    private Long totalConfirmedRoommates;

    private Long totalLandlordChats;

    private Double averageCompatibilityScore;
}
