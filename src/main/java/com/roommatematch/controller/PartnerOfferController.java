package com.roommatematch.controller;

import com.roommatematch.dto.request.PartnerOfferRequest;
import com.roommatematch.dto.response.PartnerOfferResponse;
import com.roommatematch.exception.ResourceNotFoundException;
import com.roommatematch.model.entity.User;
import com.roommatematch.repository.UserRepository;
import com.roommatematch.service.PartnerOfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@Tag(name = "Partner Offers", description = "Partner discount and offer management")
public class PartnerOfferController {

    private final PartnerOfferService partnerOfferService;
    private final UserRepository userRepository;

    private Long getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getId();
    }

    @GetMapping("/public")
    @Operation(summary = "Get all active partner offers")
    @ApiResponse(responseCode = "200", description = "Offers retrieved successfully")
    public ResponseEntity<List<PartnerOfferResponse>> getActiveOffers() {
        List<PartnerOfferResponse> offers = partnerOfferService.getActiveOffers();
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/for-match/{matchId}")
    @Operation(summary = "Get offers available for a specific match")
    @ApiResponse(responseCode = "200", description = "Offers retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Not authorized to view offers for this match")
    @ApiResponse(responseCode = "404", description = "Match not found")
    public ResponseEntity<List<PartnerOfferResponse>> getOffersForMatch(
            @PathVariable Long matchId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<PartnerOfferResponse> offers = partnerOfferService.getOffersForMatch(matchId, userId);
        return ResponseEntity.ok(offers);
    }

    @PostMapping
    @Operation(summary = "Create a new partner offer - PARTNER role only")
    @ApiResponse(responseCode = "201", description = "Offer created successfully")
    @ApiResponse(responseCode = "401", description = "Only partners can create offers")
    public ResponseEntity<PartnerOfferResponse> createOffer(
            @Valid @RequestBody PartnerOfferRequest request,
            Authentication authentication) {
        Long partnerId = getUserIdFromAuthentication(authentication);
        PartnerOfferResponse response = partnerOfferService.createOffer(partnerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate an offer - Owner only")
    @ApiResponse(responseCode = "204", description = "Offer deactivated successfully")
    @ApiResponse(responseCode = "401", description = "Only offer owner can deactivate")
    @ApiResponse(responseCode = "404", description = "Offer not found")
    public ResponseEntity<Void> deactivateOffer(
            @PathVariable Long id,
            Authentication authentication) {
        Long partnerId = getUserIdFromAuthentication(authentication);
        partnerOfferService.deactivateOffer(partnerId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @Operation(summary = "Get my offers - PARTNER role only")
    @ApiResponse(responseCode = "200", description = "Offers retrieved successfully")
    public ResponseEntity<List<PartnerOfferResponse>> getMyOffers(Authentication authentication) {
        Long partnerId = getUserIdFromAuthentication(authentication);
        List<PartnerOfferResponse> offers = partnerOfferService.getMyOffers(partnerId);
        return ResponseEntity.ok(offers);
    }
}
