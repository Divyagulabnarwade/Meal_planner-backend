package com.yourname.mealplanner.recipe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RecipeRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Servings is required")
    @Min(value = 1, message = "Servings must be at least 1")
    private Integer servings;

    @Min(value = 1, message = "Prep time must be positive")
    private Integer prepTimeMinutes;

    private String instructions;

    @Min(value = 0, message = "Calories cannot be negative")
    private Integer calories;

    private BigDecimal proteinGrams;
    private BigDecimal carbsGrams;
    private BigDecimal fatGrams;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<RecipeIngredientDto> ingredients = new ArrayList<>();
}
