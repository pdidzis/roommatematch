package com.roommatematch.service.matching;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompatibilityResult {

    private double finalScore;
    private double confidenceScore;
    private Map<String, Double> breakdown;
}
