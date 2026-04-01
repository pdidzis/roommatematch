package com.roommatematch.controller;

import com.roommatematch.dto.response.MatchResponse;
import com.roommatematch.dto.response.UserProfileResponse;
import com.roommatematch.service.MatchingService;
import com.roommatematch.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Tag(name = "Matching", description = "Hybrid roommate compatibility matching")
public class MatchController {

    private final MatchingService matchingService;
    private final UserService userService;

    private Long getCurrentUserId(Authentication authentication) {
        String email = authentication.getName();
        UserProfileResponse profile = userService.getProfileByEmail(email);
        return profile.getId();
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Get compatibility-scored match suggestions")
    @ApiResponse(responseCode = "200", description = "Suggestions retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Preferences not completed")
    public ResponseEntity<List<MatchResponse>> getSuggestions(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<MatchResponse> suggestions = matchingService.findSuggestions(userId);
        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/request/{targetUserId}")
    @Operation(summary = "Send a match request")
    @ApiResponse(responseCode = "201", description = "Match request sent successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or duplicate match")
    public ResponseEntity<MatchResponse> requestMatch(
            @PathVariable Long targetUserId,
            Authentication authentication) {
        Long requesterId = getCurrentUserId(authentication);
        MatchResponse match = matchingService.requestMatch(requesterId, targetUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(match);
    }

    @PutMapping("/{matchId}/respond")
    @Operation(summary = "Accept or decline a match request")
    @ApiResponse(responseCode = "200", description = "Response recorded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid match or already responded")
    public ResponseEntity<MatchResponse> respondToMatch(
            @PathVariable Long matchId,
            @RequestParam boolean accept,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        MatchResponse match = matchingService.respondToMatch(matchId, userId, accept);
        return ResponseEntity.ok(match);
    }

    @GetMapping("/my")
    @Operation(summary = "Get all my match requests")
    @ApiResponse(responseCode = "200", description = "Matches retrieved successfully")
    public ResponseEntity<List<MatchResponse>> getMyMatches(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<MatchResponse> matches = matchingService.getMyMatches(userId);
        return ResponseEntity.ok(matches);
    }
}
