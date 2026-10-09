package com.yourname.mealplanner.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponse {

    private Long id;
    private String title;
    private String description;
    private Integer servings;
    private Integer prepTimeMinutes;
    private String instructions;

    private Integer calories;
    private BigDecimal proteinGrams;
    private BigDecimal carbsGrams;
    private BigDecimal fatGrams;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private List<RecipeIngredientDto> ingredients = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
