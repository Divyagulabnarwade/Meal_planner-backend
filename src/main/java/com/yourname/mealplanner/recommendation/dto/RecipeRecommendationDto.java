package com.yourname.mealplanner.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRecommendationDto {

    private Long id;
    private String title;
    private String description;
    private Integer prepTimeMinutes;
    private Integer cookTimeMinutes;
    private Integer totalTimeMinutes;
    private Integer servings;
    @Builder.Default
    private List<String> dietaryTags = new ArrayList<>();
    private Integer calories;
    private BigDecimal proteinGrams;
    private BigDecimal carbsGrams;
    private BigDecimal fatGrams;
    private double matchScore; // 0.0 to 100.0%
    @Builder.Default
    private List<String> matchedIngredients = new ArrayList<>();
    @Builder.Default
    private List<String> missingIngredients = new ArrayList<>();
    private String explanation;
}
