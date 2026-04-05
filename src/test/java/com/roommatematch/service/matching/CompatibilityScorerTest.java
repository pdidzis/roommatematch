package com.roommatematch.service.matching;

import com.roommatematch.model.entity.UserPreferences;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CompatibilityScorerTest {

    private final CompatibilityScorer scorer = new CompatibilityScorer();

    private UserPreferences createPreferences(Integer minBudget, Integer maxBudget, String city,
                                               boolean pets, boolean smoking, Integer sleep,
                                               Integer cleanliness, Integer social, Integer noise,
                                               Integer guest, Integer workFromHome, LocalDate moveIn) {
        return UserPreferences.builder()
                .minBudget(minBudget)
                .maxBudget(maxBudget)
                .city(city)
                .petsAllowed(pets)
                .smokingAllowed(smoking)
                .sleepSchedule(sleep)
                .cleanlinessLevel(cleanliness)
                .socialHabits(social)
                .noiseLevel(noise)
                .guestFrequency(guest)
                .workFromHome(workFromHome)
                .moveInDate(moveIn)
                .build();
    }

    @Test
    @DisplayName("Budget mismatch returns negative score")
    void testBudgetMismatchReturnsNegative() {
        UserPreferences a = createPreferences(300, 500, "Riga", false, false, 3, 3, 3, 3, 3, 3, null);
        UserPreferences b = createPreferences(600, 800, "Riga", false, false, 3, 3, 3, 3, 3, 3, null);

        CompatibilityResult result = scorer.calculateScore(a, b);

        assertEquals(-1.0, result.getFinalScore());
    }

    @Test
    @DisplayName("Different cities returns negative score")
    void testCityMismatchReturnsNegative() {
        UserPreferences a = createPreferences(300, 600, "Riga", false, false, 3, 3, 3, 3, 3, 3, null);
        UserPreferences b = createPreferences(300, 600, "Tallinn", false, false, 3, 3, 3, 3, 3, 3, null);

        CompatibilityResult result = scorer.calculateScore(a, b);

        assertEquals(-1.0, result.getFinalScore());
    }

    @Test
    @DisplayName("Pet policy mismatch returns negative score")
    void testPetsMismatchReturnsNegative() {
        UserPreferences a = createPreferences(300, 600, "Riga", true, false, 3, 3, 3, 3, 3, 3, null);
        UserPreferences b = createPreferences(300, 600, "Riga", false, false, 3, 3, 3, 3, 3, 3, null);

        CompatibilityResult result = scorer.calculateScore(a, b);

        assertEquals(-1.0, result.getFinalScore());
    }

    @Test
    @DisplayName("Smoking policy mismatch returns negative score")
    void testSmokingMismatchReturnsNegative() {
        UserPreferences a = createPreferences(300, 600, "Riga", false, true, 3, 3, 3, 3, 3, 3, null);
        UserPreferences b = createPreferences(300, 600, "Riga", false, false, 3, 3, 3, 3, 3, 3, null);

        CompatibilityResult result = scorer.calculateScore(a, b);

        assertEquals(-1.0, result.getFinalScore());
    }

    @Test
    @DisplayName("Move-in dates too far apart returns negative score")
    void testMoveInDateTooFarReturnsNegative() {
        LocalDate now = LocalDate.now();
        UserPreferences a = createPreferences(300, 600, "Riga", false, false, 3, 3, 3, 3, 3, 3, now);
        UserPreferences b = createPreferences(300, 600, "Riga", false, false, 3, 3, 3, 3, 3, 3, now.plusDays(90));

        CompatibilityResult result = scorer.calculateScore(a, b);

        assertEquals(-1.0, result.getFinalScore());
    }

    @Test
    @DisplayName("Identical preferences return score of 1.0")
    void testIdenticalPreferencesReturnPerfectScore() {
        UserPreferences a = createPreferences(300, 600, "Riga", false, false, 3, 3, 3, 3, 3, 3, null);
        UserPreferences b = createPreferences(300, 600, "Riga", false, false, 3, 3, 3, 3, 3, 3, null);

        CompatibilityResult result = scorer.calculateScore(a, b);

        assertEquals(1.0, result.getFinalScore());
        assertEquals(1.0, result.getConfidenceScore());
    }

    @Test
    @DisplayName("Partial compatibility returns score between 0.3 and 0.9")
    void testPartialCompatibilityInRange() {
        UserPreferences a = createPreferences(300, 600, "Riga", false, false, 2, 4, 2, 2, 2, 2, null);
        UserPreferences b = createPreferences(300, 600, "Riga", false, false, 4, 2, 4, 4, 4, 4, null);

        CompatibilityResult result = scorer.calculateScore(a, b);

        assertTrue(result.getFinalScore() >= 0.3, "Score should be >= 0.3 but was " + result.getFinalScore());
        assertTrue(result.getFinalScore() <= 0.9, "Score should be <= 0.9 but was " + result.getFinalScore());
    }

    @Test
    @DisplayName("Score breakdown map contains all expected keys")
    void testScoreBreakdownContainsAllFactors() {
        UserPreferences a = createPreferences(300, 600, "Riga", false, false, 3, 3, 3, 3, 3, 3, null);
        UserPreferences b = createPreferences(300, 600, "Riga", false, false, 3, 3, 3, 3, 3, 3, null);

        CompatibilityResult result = scorer.calculateScore(a, b);

        assertNotNull(result.getBreakdown());
        Map<String, Double> breakdown = result.getBreakdown();
        assertTrue(breakdown.containsKey("sleepSchedule"), "Breakdown should contain sleepSchedule");
        assertTrue(breakdown.containsKey("cleanliness"), "Breakdown should contain cleanliness");
        assertTrue(breakdown.containsKey("socialHabits"), "Breakdown should contain socialHabits");
        assertTrue(breakdown.containsKey("noiseLevel"), "Breakdown should contain noiseLevel");
        assertTrue(breakdown.containsKey("guestFrequency"), "Breakdown should contain guestFrequency");
        assertTrue(breakdown.containsKey("workFromHome"), "Breakdown should contain workFromHome");
        assertTrue(breakdown.containsKey("budgetCompatibility"), "Breakdown should contain budgetCompatibility");
    }

    @Test
    @DisplayName("Null preferences return negative score")
    void testNullPreferencesReturnNegative() {
        CompatibilityResult result = scorer.calculateScore(null, null);

        assertEquals(-1.0, result.getFinalScore());
    }

    @Test
    @DisplayName("Sleep schedule max difference reduces score significantly")
    void testMaxSleepDifferenceReducesScore() {
        UserPreferences a = createPreferences(300, 600, "Riga", false, false, 1, 3, 3, 3, 3, 3, null);
        UserPreferences b = createPreferences(300, 600, "Riga", false, false, 5, 3, 3, 3, 3, 3, null);

        CompatibilityResult result = scorer.calculateScore(a, b);

        assertTrue(result.getFinalScore() < 0.8,
                "Score should be < 0.8 due to sleep schedule penalty but was " + result.getFinalScore());
    }
}
