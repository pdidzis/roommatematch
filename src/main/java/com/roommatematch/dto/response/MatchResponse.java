package com.roommatematch.dto.response;

import com.roommatematch.model.enums.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {

    private Long matchId;
    private Double compatibilityScore;
    private Double confidenceScore;
    private MatchStatus status;
    private UserSummary otherUser;
    private Map<String, Double> scoreBreakdown;
    private String createdAt;
}
