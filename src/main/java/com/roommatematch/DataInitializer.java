package com.roommatematch;

import com.roommatematch.model.entity.Listing;
import com.roommatematch.model.entity.PartnerOffer;
import com.roommatematch.model.entity.User;
import com.roommatematch.model.entity.UserPreferences;
import com.roommatematch.model.enums.ListingStatus;
import com.roommatematch.model.enums.UserRole;
import com.roommatematch.repository.ListingRepository;
import com.roommatematch.repository.PartnerOfferRepository;
import com.roommatematch.repository.UserPreferencesRepository;
import com.roommatematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final ListingRepository listingRepository;
    private final PartnerOfferRepository partnerOfferRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already has data, skipping initialization");
            return;
        }

        log.info("Initializing development data...");

        // Create ADMIN user
        User admin = createUser("admin@roommatematch.com", "Admin1234!",
                "Admin", "User", UserRole.ADMIN);

        // Create LANDLORD users
        User landlord1 = createUser("john.landlord@test.com", "Landlord1234!",
                "John", "Miller", UserRole.LANDLORD);
        User landlord2 = createUser("sarah.landlord@test.com", "Landlord1234!",
                "Sarah", "Johnson", UserRole.LANDLORD);
        User landlord3 = createUser("mike.landlord@test.com", "Landlord1234!",
                "Mike", "Brown", UserRole.LANDLORD);

        // Create listings for Landlord 1
        createListing(landlord1, "Modern Studio in Riga Center",
                "Bright studio apartment near Old Town",
                "Brivibas iela 45", "Riga", "Latvia",
                BigDecimal.valueOf(450), LocalDate.now().plusMonths(1),
                1, 1, false, false, ListingStatus.ACTIVE, true);

        createListing(landlord1, "Cozy Room in Shared Apartment",
                "Furnished room in quiet neighborhood",
                "Terbatas iela 12", "Riga", "Latvia",
                BigDecimal.valueOf(320), LocalDate.now().plusWeeks(2),
                3, 1, true, false, ListingStatus.ACTIVE, true);

        // Create listings for Landlord 2
        createListing(landlord2, "Spacious Room Near University",
                "Perfect for students, close to RTU",
                "Kronvalda bulvaris 4", "Riga", "Latvia",
                BigDecimal.valueOf(380), LocalDate.now().plusMonths(1),
                4, 2, false, false, ListingStatus.ACTIVE, true);

        createListing(landlord2, "Budget Room in Quiet Area",
                "Affordable room in residential area",
                "Maskavas iela 88", "Riga", "Latvia",
                BigDecimal.valueOf(280), LocalDate.now().plusWeeks(3),
                2, 1, true, true, ListingStatus.PENDING, false);

        // Create listings for Landlord 3
        createListing(landlord3, "Premium Apartment Share",
                "Luxury apartment in Art Nouveau district",
                "Alberta iela 7", "Riga", "Latvia",
                BigDecimal.valueOf(550), LocalDate.now().plusMonths(1),
                2, 1, false, false, ListingStatus.ACTIVE, true);

        createListing(landlord3, "Student Friendly Room",
                "Great location for university students",
                "Dzirnavu iela 33", "Riga", "Latvia",
                BigDecimal.valueOf(250), LocalDate.now().plusWeeks(1),
                5, 2, true, false, ListingStatus.ACTIVE, true);

        // Create TENANT users - Group A (Highly compatible)
        User tenant1 = createTenantWithPrefs("alex.tenant@test.com", "Alex", "Peterson",
                300, 600, "Riga", LocalDate.now().plusMonths(1),
                false, false, 2, 4, 3, 2, 2, 2);

        User tenant2 = createTenantWithPrefs("emma.tenant@test.com", "Emma", "Wilson",
                350, 650, "Riga", LocalDate.now().plusMonths(1).plusWeeks(1),
                false, false, 2, 4, 3, 2, 2, 2);

        User tenant3 = createTenantWithPrefs("lucas.tenant@test.com", "Lucas", "Garcia",
                280, 550, "Riga", LocalDate.now().plusMonths(1),
                false, false, 3, 3, 4, 3, 3, 3);

        User tenant4 = createTenantWithPrefs("sofia.tenant@test.com", "Sofia", "Martinez",
                300, 580, "Riga", LocalDate.now().plusMonths(1).plusWeeks(2),
                false, false, 3, 3, 4, 3, 3, 3);

        User tenant5 = createTenantWithPrefs("noah.tenant@test.com", "Noah", "Anderson",
                400, 700, "Riga", LocalDate.now().plusMonths(2),
                true, false, 4, 5, 2, 5, 1, 1);

        // Create TENANT users - Group B (Compatible with each other)
        User tenant6 = createTenantWithPrefs("olivia.tenant@test.com", "Olivia", "Taylor",
                400, 700, "Riga", LocalDate.now().plusMonths(2),
                true, false, 4, 5, 2, 5, 1, 1);

        User tenant7 = createTenantWithPrefs("liam.tenant@test.com", "Liam", "Thomas",
                200, 400, "Riga", LocalDate.now().plusWeeks(3),
                true, true, 5, 2, 5, 1, 5, 5);

        User tenant8 = createTenantWithPrefs("ava.tenant@test.com", "Ava", "Jackson",
                200, 420, "Riga", LocalDate.now().plusWeeks(2),
                true, true, 5, 2, 5, 1, 5, 5);

        // Create TENANT users - Group C (Incompatible - different cities)
        User tenant9 = createTenantWithPrefs("ethan.tenant@test.com", "Ethan", "White",
                800, 1200, "Tallinn", LocalDate.now().plusMonths(3),
                false, false, 3, 3, 3, 3, 3, 3);

        User tenant10 = createTenantWithPrefs("isabella.tenant@test.com", "Isabella", "Harris",
                900, 1500, "Vilnius", LocalDate.now().plusMonths(4),
                true, true, 1, 1, 1, 1, 1, 1);

        // Create PARTNER users with offers
        User partner1 = createUser("pizza.partner@test.com", "Partner1234!",
                "Pizza", "Palace", UserRole.PARTNER);

        PartnerOffer offer1 = PartnerOffer.builder()
                .partner(partner1)
                .title("15% off for confirmed roommates")
                .description("Show your RoommateMatch confirmation at Pizza Palace for 15% discount")
                .discountPercent(15)
                .validUntil(LocalDate.now().plusMonths(6))
                .isActive(true)
                .build();
        partnerOfferRepository.save(offer1);

        User partner2 = createUser("cafe.partner@test.com", "Partner1234!",
                "Coffee", "Corner", UserRole.PARTNER);

        PartnerOffer offer2 = PartnerOffer.builder()
                .partner(partner2)
                .title("Buy one get one free coffee")
                .description("Present your RoommateMatch app at Coffee Corner for BOGO coffee deal")
                .discountPercent(50)
                .validUntil(LocalDate.now().plusMonths(3))
                .isActive(true)
                .build();
        partnerOfferRepository.save(offer2);

        log.info("Dev data initialized successfully!");
        log.info("Admin: admin@roommatematch.com / Admin1234!");
        log.info("Landlords: john.landlord@test.com, sarah.landlord@test.com, mike.landlord@test.com / Landlord1234!");
        log.info("Tenants: alex.tenant@test.com ... isabella.tenant@test.com / Tenant1234!");
        log.info("Partners: pizza.partner@test.com, cafe.partner@test.com / Partner1234!");
    }

    private User createUser(String email, String password, String firstName,
                            String lastName, UserRole role) {
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .isVerified(true)
                .isActive(true)
                .build();
        return userRepository.save(user);
    }

    private User createTenantWithPrefs(String email, String firstName, String lastName,
                                        int minBudget, int maxBudget, String city,
                                        LocalDate moveInDate, boolean petsAllowed,
                                        boolean smokingAllowed, int sleepSchedule,
                                        int cleanlinessLevel, int socialHabits,
                                        int workFromHome, int guestFrequency,
                                        int noiseLevel) {
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("Tenant1234!"))
                .firstName(firstName)
                .lastName(lastName)
                .role(UserRole.TENANT)
                .isVerified(true)
                .isActive(true)
                .build();
        user = userRepository.save(user);

        UserPreferences prefs = UserPreferences.builder()
                .user(user)
                .minBudget(minBudget)
                .maxBudget(maxBudget)
                .city(city)
                .moveInDate(moveInDate)
                .petsAllowed(petsAllowed)
                .smokingAllowed(smokingAllowed)
                .sleepSchedule(sleepSchedule)
                .cleanlinessLevel(cleanlinessLevel)
                .socialHabits(socialHabits)
                .workFromHome(workFromHome)
                .guestFrequency(guestFrequency)
                .noiseLevel(noiseLevel)
                .build();
        userPreferencesRepository.save(prefs);

        return user;
    }

    private Listing createListing(User landlord, String title, String description,
                                   String address, String city, String country,
                                   BigDecimal monthlyRent, LocalDate availableFrom,
                                   int totalRooms, int availableRooms,
                                   boolean petsAllowed, boolean smokingAllowed,
                                   ListingStatus status, boolean isVerified) {
        Listing listing = Listing.builder()
                .landlord(landlord)
                .title(title)
                .description(description)
                .address(address)
                .city(city)
                .country(country)
                .monthlyRent(monthlyRent)
                .availableFrom(availableFrom)
                .totalRooms(totalRooms)
                .availableRooms(availableRooms)
                .petsAllowed(petsAllowed)
                .smokingAllowed(smokingAllowed)
                .status(status)
                .isVerified(isVerified)
                .build();
        return listingRepository.save(listing);
    }
}
