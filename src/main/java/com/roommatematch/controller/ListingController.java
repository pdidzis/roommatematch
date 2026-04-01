package com.roommatematch.controller;

import com.roommatematch.dto.request.ListingRequest;
import com.roommatematch.dto.response.ListingResponse;
import com.roommatematch.exception.ResourceNotFoundException;
import com.roommatematch.model.entity.User;
import com.roommatematch.repository.UserRepository;
import com.roommatematch.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
@Tag(name = "Listings", description = "Property listing management")
public class ListingController {

    private final ListingService listingService;
    private final UserRepository userRepository;

    private Long getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getId();
    }

    @GetMapping("/public")
    @Operation(summary = "Get all public verified listings")
    @ApiResponse(responseCode = "200", description = "Listings retrieved successfully")
    public ResponseEntity<List<ListingResponse>> getPublicListings(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal maxRent,
            @RequestParam(required = false) Boolean petsAllowed,
            @RequestParam(required = false) Boolean smokingAllowed) {

        List<ListingResponse> listings = listingService.getPublicListings(
                city, maxRent, petsAllowed, smokingAllowed);
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "Get single listing details")
    @ApiResponse(responseCode = "200", description = "Listing retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Listing not found")
    public ResponseEntity<ListingResponse> getPublicListingById(@PathVariable Long id) {
        ListingResponse listing = listingService.getListingById(id);
        return ResponseEntity.ok(listing);
    }

    @PostMapping
    @Operation(summary = "Create a new listing")
    @ApiResponse(responseCode = "201", description = "Listing created successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Only landlords can create listings")
    public ResponseEntity<ListingResponse> createListing(
            @Valid @RequestBody ListingRequest request,
            Authentication authentication) {

        Long landlordId = getUserIdFromAuthentication(authentication);
        ListingResponse listing = listingService.createListing(landlordId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(listing);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a listing")
    @ApiResponse(responseCode = "200", description = "Listing updated successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "You can only edit your own listings")
    @ApiResponse(responseCode = "404", description = "Listing not found")
    public ResponseEntity<ListingResponse> updateListing(
            @PathVariable Long id,
            @Valid @RequestBody ListingRequest request,
            Authentication authentication) {

        Long landlordId = getUserIdFromAuthentication(authentication);
        ListingResponse listing = listingService.updateListing(landlordId, id, request);
        return ResponseEntity.ok(listing);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Archive a listing")
    @ApiResponse(responseCode = "204", description = "Listing archived successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "You can only archive your own listings")
    @ApiResponse(responseCode = "404", description = "Listing not found")
    public ResponseEntity<Void> archiveListing(
            @PathVariable Long id,
            Authentication authentication) {

        Long landlordId = getUserIdFromAuthentication(authentication);
        listingService.archiveListing(landlordId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @Operation(summary = "Get my listings")
    @ApiResponse(responseCode = "200", description = "Listings retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<List<ListingResponse>> getMyListings(Authentication authentication) {
        Long landlordId = getUserIdFromAuthentication(authentication);
        List<ListingResponse> listings = listingService.getMyListings(landlordId);
        return ResponseEntity.ok(listings);
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload listing photos")
    @ApiResponse(responseCode = "200", description = "Photos uploaded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid file type or size")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "You can only upload photos to your own listings")
    @ApiResponse(responseCode = "404", description = "Listing not found")
    public ResponseEntity<ListingResponse> uploadListingPhotos(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication) {

        Long landlordId = getUserIdFromAuthentication(authentication);
        ListingResponse listing = listingService.uploadListingPhotos(landlordId, id, files);
        return ResponseEntity.ok(listing);
    }

    @PutMapping("/{id}/verify")
    @Operation(summary = "Verify a listing - Admin only")
    @ApiResponse(responseCode = "200", description = "Listing verified successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Only admins can verify listings")
    @ApiResponse(responseCode = "404", description = "Listing not found")
    public ResponseEntity<ListingResponse> verifyListing(
            @PathVariable Long id,
            Authentication authentication) {

        Long adminId = getUserIdFromAuthentication(authentication);
        ListingResponse listing = listingService.verifyListing(id, adminId);
        return ResponseEntity.ok(listing);
    }
}
