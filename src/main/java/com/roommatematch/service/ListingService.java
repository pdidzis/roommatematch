package com.roommatematch.service;

import com.roommatematch.dto.request.ListingRequest;
import com.roommatematch.dto.response.ListingResponse;
import com.roommatematch.exception.BusinessLogicException;
import com.roommatematch.exception.ResourceNotFoundException;
import com.roommatematch.exception.UnauthorizedException;
import com.roommatematch.model.entity.Listing;
import com.roommatematch.model.entity.User;
import com.roommatematch.model.enums.ListingStatus;
import com.roommatematch.model.enums.UserRole;
import com.roommatematch.repository.ListingRepository;
import com.roommatematch.repository.UserRepository;
import com.roommatematch.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final InputSanitizer sanitizer;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Transactional
    public ListingResponse createListing(Long landlordId, ListingRequest request) {
        User landlord = userRepository.findById(landlordId)
                .orElseThrow(() -> new ResourceNotFoundException("User", landlordId));

        if (landlord.getRole() != UserRole.LANDLORD) {
            throw new UnauthorizedException("Only landlords can create listings");
        }

        Listing listing = Listing.builder()
                .landlord(landlord)
                .title(sanitizer.sanitize(request.getTitle()))
                .description(sanitizer.sanitize(request.getDescription()))
                .address(sanitizer.sanitize(request.getAddress()))
                .city(request.getCity())
                .country(request.getCountry())
                .monthlyRent(request.getMonthlyRent())
                .availableFrom(request.getAvailableFrom())
                .totalRooms(request.getTotalRooms())
                .availableRooms(request.getAvailableRooms())
                .petsAllowed(request.isPetsAllowed())
                .smokingAllowed(request.isSmokingAllowed())
                .status(ListingStatus.PENDING)
                .isVerified(false)
                .build();

        Listing savedListing = listingRepository.save(listing);
        log.info("Listing created by landlord: {}", landlordId);

        return mapToListingResponse(savedListing);
    }

    @Transactional
    public ListingResponse updateListing(Long landlordId, Long listingId, ListingRequest request) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));

        if (!listing.getLandlord().getId().equals(landlordId)) {
            throw new UnauthorizedException("You can only edit your own listings");
        }

        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setAddress(request.getAddress());
        listing.setCity(request.getCity());
        listing.setCountry(request.getCountry());
        listing.setMonthlyRent(request.getMonthlyRent());
        listing.setAvailableFrom(request.getAvailableFrom());
        listing.setTotalRooms(request.getTotalRooms());
        listing.setAvailableRooms(request.getAvailableRooms());
        listing.setPetsAllowed(request.isPetsAllowed());
        listing.setSmokingAllowed(request.isSmokingAllowed());

        Listing updatedListing = listingRepository.save(listing);
        return mapToListingResponse(updatedListing);
    }

    @Transactional
    public void archiveListing(Long landlordId, Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));

        if (!listing.getLandlord().getId().equals(landlordId)) {
            throw new UnauthorizedException("You can only archive your own listings");
        }

        listing.setStatus(ListingStatus.ARCHIVED);
        listingRepository.save(listing);
        log.info("Listing archived: {}", listingId);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getMyListings(Long landlordId) {
        User landlord = userRepository.findById(landlordId)
                .orElseThrow(() -> new ResourceNotFoundException("User", landlordId));

        return listingRepository.findByLandlord(landlord).stream()
                .map(this::mapToListingResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getPublicListings(String city, BigDecimal maxRent,
                                                    Boolean petsAllowed, Boolean smokingAllowed) {
        List<Listing> listings = listingRepository
                .findByStatusAndIsVerifiedOrderByCreatedAtDesc(ListingStatus.ACTIVE, true);

        return listings.stream()
                .filter(listing -> city == null ||
                        listing.getCity().equalsIgnoreCase(city))
                .filter(listing -> maxRent == null ||
                        listing.getMonthlyRent().compareTo(maxRent) <= 0)
                .filter(listing -> petsAllowed == null ||
                        listing.isPetsAllowed() == petsAllowed)
                .filter(listing -> smokingAllowed == null ||
                        listing.isSmokingAllowed() == smokingAllowed)
                .sorted(Comparator.comparing(Listing::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::mapToListingResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ListingResponse getListingById(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));

        return mapToListingResponse(listing);
    }

    @Transactional
    public ListingResponse verifyListing(Long listingId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adminId));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only admins can verify listings");
        }

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));

        listing.setVerified(true);
        listing.setStatus(ListingStatus.ACTIVE);

        Listing verifiedListing = listingRepository.save(listing);
        log.info("Listing verified by admin {}: {}", adminId, listingId);

        notificationService.createNotification(
                listing.getLandlord().getId(),
                "Listing verified! ✅",
                "Your listing '" + listing.getTitle() + "' is now live on RoommateMatch.",
                "LISTING_VERIFIED",
                listing.getId()
        );

        return mapToListingResponse(verifiedListing);
    }

    @Transactional
    public ListingResponse uploadListingPhotos(Long landlordId, Long listingId,
                                                List<MultipartFile> files) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));

        if (!listing.getLandlord().getId().equals(landlordId)) {
            throw new UnauthorizedException("You can only upload photos to your own listings");
        }

        List<String> photoPaths = new ArrayList<>();

        for (MultipartFile file : files) {
            validateFile(file);

            String fileName = UUID.randomUUID().toString() + "_" +
                    sanitizeFileName(file.getOriginalFilename());
            Path uploadDir = Paths.get("./uploads/listings/" + listingId);

            try {
                Files.createDirectories(uploadDir);
                Path filePath = uploadDir.resolve(fileName);
                Files.write(filePath, file.getBytes());
                photoPaths.add(filePath.toString());
            } catch (IOException e) {
                log.error("Failed to upload file: {}", e.getMessage());
                throw new BusinessLogicException("Failed to upload file: " + file.getOriginalFilename());
            }
        }

        String existingPhotos = listing.getPhotoUrls();
        String newPhotos = String.join(",", photoPaths);

        if (existingPhotos != null && !existingPhotos.isEmpty()) {
            listing.setPhotoUrls(existingPhotos + "," + newPhotos);
        } else {
            listing.setPhotoUrls(newPhotos);
        }

        Listing updatedListing = listingRepository.save(listing);
        return mapToListingResponse(updatedListing);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessLogicException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessLogicException("File size exceeds maximum allowed size of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessLogicException("Only JPG, PNG, GIF, and WebP images are allowed");
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unknown";
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private ListingResponse mapToListingResponse(Listing listing) {
        User landlord = listing.getLandlord();

        List<String> photoUrlList;
        if (listing.getPhotoUrls() != null && !listing.getPhotoUrls().isEmpty()) {
            photoUrlList = Arrays.asList(listing.getPhotoUrls().split(","));
        } else {
            photoUrlList = new ArrayList<>();
        }

        long daysUntilAvailable = 0;
        if (listing.getAvailableFrom() != null) {
            daysUntilAvailable = Math.max(0,
                    ChronoUnit.DAYS.between(LocalDate.now(), listing.getAvailableFrom()));
        }

        String createdAtStr = listing.getCreatedAt() != null
                ? listing.getCreatedAt().toString()
                : null;

        return ListingResponse.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .address(listing.getAddress())
                .city(listing.getCity())
                .country(listing.getCountry())
                .monthlyRent(listing.getMonthlyRent())
                .availableFrom(listing.getAvailableFrom())
                .totalRooms(listing.getTotalRooms())
                .availableRooms(listing.getAvailableRooms())
                .petsAllowed(listing.isPetsAllowed())
                .smokingAllowed(listing.isSmokingAllowed())
                .photoUrls(photoUrlList)
                .status(listing.getStatus())
                .isVerified(listing.isVerified())
                .landlordName(landlord.getFirstName() + " " + landlord.getLastName())
                .landlordEmail(landlord.getEmail())
                .landlordPhone(landlord.getPhoneNumber())
                .createdAt(createdAtStr)
                .daysUntilAvailable(daysUntilAvailable)
                .build();
    }
}
