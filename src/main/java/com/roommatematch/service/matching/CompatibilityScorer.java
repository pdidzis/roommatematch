package com.roommatematch.service.matching;

import com.roommatematch.model.entity.UserPreferences;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class CompatibilityScorer {

    private static final double WEIGHT_SLEEP_SCHEDULE = 0.25;
    private static final double WEIGHT_CLEANLINESS = 0.25;
    private static final double WEIGHT_SOCIAL_HABITS = 0.20;
    private static final double WEIGHT_NOISE_LEVEL = 0.15;
    private static final double WEIGHT_GUEST_FREQUENCY = 0.10;
    private static final double WEIGHT_WORK_FROM_HOME = 0.05;

    public CompatibilityResult calculateScore(UserPreferences a, UserPreferences b) {
        // Step 1 - Null check
        if (a == null || b == null) {
            return CompatibilityResult.builder()
                    .finalScore(-1.0)
                    .confidenceScore(0.0)
                    .breakdown(new HashMap<>())
                    .build();
        }

        // Step 2 - Hard filters
        if (!passesHardFilters(a, b)) {
            return CompatibilityResult.builder()
                    .finalScore(-1.0)
                    .confidenceScore(0.0)
                    .breakdown(new HashMap<>())
                    .build();
        }

        // Step 3 - Budget compatibility ratio
        double budgetScore = calculateBudgetScore(a, b);

        // Step 4 - Dynamic weight calculation
        Map<String, Double> weightsA = calculateDynamicWeights(a);
        Map<String, Double> weightsB = calculateDynamicWeights(b);

        // Step 5 & 6 - Factor scoring with bidirectional scoring
        double scoreFromA = calculateWeightedScore(a, b, weightsA);
        double scoreFromB = calculateWeightedScore(a, b, weightsB);
        double finalScore = (scoreFromA + scoreFromB) / 2.0;

        // Step 7 - Include budget score
        finalScore = (finalScore * 0.85) + (budgetScore * 0.15);

        // Step 8 - Confidence score
        double confidenceScore = calculateConfidenceScore(a, b);
        if (confidenceScore < 0.5) {
            finalScore *= (0.9 + confidenceScore * 0.2);
        }

        // Step 9 - Build breakdown map
        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("sleepSchedule", factorScore(a.getSleepSchedule(), b.getSleepSchedule()));
        breakdown.put("cleanliness", factorScore(a.getCleanlinessLevel(), b.getCleanlinessLevel()));
        breakdown.put("socialHabits", factorScore(a.getSocialHabits(), b.getSocialHabits()));
        breakdown.put("noiseLevel", factorScore(a.getNoiseLevel(), b.getNoiseLevel()));
        breakdown.put("guestFrequency", factorScore(a.getGuestFrequency(), b.getGuestFrequency()));
        breakdown.put("workFromHome", factorScore(a.getWorkFromHome(), b.getWorkFromHome()));
        breakdown.put("budgetCompatibility", budgetScore);

        // Step 10 - Round final score
        return CompatibilityResult.builder()
                .finalScore(Math.round(finalScore * 100.0) / 100.0)
                .confidenceScore(Math.round(confidenceScore * 100.0) / 100.0)
                .breakdown(breakdown)
                .build();
    }

    private boolean passesHardFilters(UserPreferences a, UserPreferences b) {
        // Budget overlap check
        if (a.getMinBudget() == null || a.getMaxBudget() == null ||
            b.getMinBudget() == null || b.getMaxBudget() == null) {
            return false;
        }
        if (a.getMaxBudget() < b.getMinBudget() || a.getMinBudget() > b.getMaxBudget()) {
            return false;
        }

        // City match check
        if (a.getCity() == null || b.getCity() == null) {
            return false;
        }
        if (!a.getCity().equalsIgnoreCase(b.getCity())) {
            return false;
        }

        // Pet policy check
        if (a.isPetsAllowed() != b.isPetsAllowed()) {
            return false;
        }

        // Smoking policy check
        if (a.isSmokingAllowed() != b.isSmokingAllowed()) {
            return false;
        }

        // Move-in date check
        if (a.getMoveInDate() != null && b.getMoveInDate() != null) {
            long daysDiff = Math.abs(ChronoUnit.DAYS.between(a.getMoveInDate(), b.getMoveInDate()));
            if (daysDiff > 60) {
                return false;
            }
        }

        return true;
    }

    private double calculateBudgetScore(UserPreferences a, UserPreferences b) {
        double budgetOverlap = Math.min(a.getMaxBudget(), b.getMaxBudget())
                - Math.max(a.getMinBudget(), b.getMinBudget());
        double avgRange = ((a.getMaxBudget() - a.getMinBudget()) +
                (b.getMaxBudget() - b.getMinBudget())) / 2.0;
        return avgRange > 0 ? Math.min(budgetOverlap / avgRange, 1.0) : 1.0;
    }

    private Map<String, Double> calculateDynamicWeights(UserPreferences prefs) {
        Map<String, Double> weights = new HashMap<>();
        weights.put("sleepSchedule", WEIGHT_SLEEP_SCHEDULE);
        weights.put("cleanliness", WEIGHT_CLEANLINESS);
        weights.put("socialHabits", WEIGHT_SOCIAL_HABITS);
        weights.put("noiseLevel", WEIGHT_NOISE_LEVEL);
        weights.put("guestFrequency", WEIGHT_GUEST_FREQUENCY);
        weights.put("workFromHome", WEIGHT_WORK_FROM_HOME);

        // Adjust weights based on user's values
        adjustWeight(weights, "sleepSchedule", prefs.getSleepSchedule());
        adjustWeight(weights, "cleanliness", prefs.getCleanlinessLevel());
        adjustWeight(weights, "socialHabits", prefs.getSocialHabits());
        adjustWeight(weights, "noiseLevel", prefs.getNoiseLevel());
        adjustWeight(weights, "guestFrequency", prefs.getGuestFrequency());
        adjustWeight(weights, "workFromHome", prefs.getWorkFromHome());

        // Normalize weights to sum to 1.0
        double total = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total > 0) {
            weights.replaceAll((k, v) -> v / total);
        }

        return weights;
    }

    private void adjustWeight(Map<String, Double> weights, String factor, Integer value) {
        if (value == null) return;
        if (value == 5) {
            weights.put(factor, weights.get(factor) * 1.2);
        } else if (value == 1) {
            weights.put(factor, weights.get(factor) * 0.8);
        }
    }

    private double calculateWeightedScore(UserPreferences a, UserPreferences b,
                                          Map<String, Double> weights) {
        double score = 0.0;
        score += weights.get("sleepSchedule") * factorScore(a.getSleepSchedule(), b.getSleepSchedule());
        score += weights.get("cleanliness") * factorScore(a.getCleanlinessLevel(), b.getCleanlinessLevel());
        score += weights.get("socialHabits") * factorScore(a.getSocialHabits(), b.getSocialHabits());
        score += weights.get("noiseLevel") * factorScore(a.getNoiseLevel(), b.getNoiseLevel());
        score += weights.get("guestFrequency") * factorScore(a.getGuestFrequency(), b.getGuestFrequency());
        score += weights.get("workFromHome") * factorScore(a.getWorkFromHome(), b.getWorkFromHome());
        return score;
    }

    private double factorScore(Integer a, Integer b) {
        if (a == null || b == null) {
            return 0.5;
        }

        double rawSimilarity = 1.0 - (Math.abs(a - b) / 4.0);

        // Apply penalty for extreme differences
        int diff = Math.abs(a - b);
        if (diff >= 3) {
            rawSimilarity *= 0.7; // 30% penalty for dealbreaker level
        }
        if (diff == 4) {
            rawSimilarity *= 0.5; // Additional 50% for maximum diff
        }

        return rawSimilarity;
    }

    private double calculateConfidenceScore(UserPreferences a, UserPreferences b) {
        int filledA = countFilledFields(a);
        int filledB = countFilledFields(b);
        return (filledA + filledB) / 12.0;
    }

    private int countFilledFields(UserPreferences prefs) {
        int count = 0;
        if (prefs.getSleepSchedule() != null) count++;
        if (prefs.getCleanlinessLevel() != null) count++;
        if (prefs.getSocialHabits() != null) count++;
        if (prefs.getNoiseLevel() != null) count++;
        if (prefs.getGuestFrequency() != null) count++;
        if (prefs.getWorkFromHome() != null) count++;
        return count;
    }
}
