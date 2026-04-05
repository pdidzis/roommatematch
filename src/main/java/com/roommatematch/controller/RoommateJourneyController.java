package com.roommatematch.controller;

import com.roommatematch.dto.response.ChatRoomResponse;
import com.roommatematch.dto.response.ListingResponse;
import com.roommatematch.dto.response.MatchResponse;
import com.roommatematch.exception.ResourceNotFoundException;
import com.roommatematch.model.entity.User;
import com.roommatematch.repository.UserRepository;
import com.roommatematch.service.RoommateJourneyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/journey")
@RequiredArgsConstructor
@Tag(name = "Roommate Journey", description = "Complete roommate matching journey endpoints")
public class RoommateJourneyController {

    private final RoommateJourneyService journeyService;
    private final UserRepository userRepository;

    private Long getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getId();
    }

    @PutMapping("/matches/{matchId}/confirm-roommate")
    @Operation(summary = "Confirm someone as your roommate")
    @ApiResponse(responseCode = "200", description = "Roommate confirmed successfully")
    @ApiResponse(responseCode = "400", description = "Match must be accepted first")
    @ApiResponse(responseCode = "404", description = "Match not found")
    public ResponseEntity<MatchResponse> confirmRoommate(
            @PathVariable Long matchId,
            Authentication authentication) {
        Long currentUserId = getUserIdFromAuthentication(authentication);
        MatchResponse response = journeyService.confirmRoommate(matchId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/listings")
    @Operation(summary = "Browse listings - requires confirmed roommate")
    @ApiResponse(responseCode = "200", description = "Listings retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Need confirmed roommate to browse")
    public ResponseEntity<List<ListingResponse>> getListingsForRoommates(
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<ListingResponse> listings = journeyService.getListingsForRoommates(userId);
        return ResponseEntity.ok(listings);
    }

    @PostMapping("/listings/{listingId}/interest")
    @Operation(summary = "Express interest in a listing")
    @ApiResponse(responseCode = "200", description = "Interest registered")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "409", description = "Already expressed interest")
    public ResponseEntity<String> expressInterestInListing(
            @PathVariable Long listingId,
            @RequestParam Long matchId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        String result = journeyService.expressInterestInListing(userId, listingId, matchId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/listings/my-interests")
    @Operation(summary = "Get listings I am interested in")
    @ApiResponse(responseCode = "200", description = "Interests retrieved successfully")
    public ResponseEntity<List<ListingResponse>> getMyInterests(
            @RequestParam Long matchId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<ListingResponse> interests = journeyService.getMyInterests(userId, matchId);
        return ResponseEntity.ok(interests);
    }

    @GetMapping("/landlord/chats")
    @Operation(summary = "Get all chats for landlord")
    @ApiResponse(responseCode = "200", description = "Chats retrieved successfully")
    public ResponseEntity<List<ChatRoomResponse>> getLandlordChats(
            Authentication authentication) {
        Long landlordId = getUserIdFromAuthentication(authentication);
        List<ChatRoomResponse> chats = journeyService.getLandlordChats(landlordId);
        return ResponseEntity.ok(chats);
    }
}
