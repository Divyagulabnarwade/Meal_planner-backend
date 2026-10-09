package com.yourname.mealplanner.mealplan.dto;

import com.yourname.mealplanner.mealplan.MealType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanRequest {

    @NotNull(message = "Recipe ID is required")
    private Long recipeId;

    @NotNull(message = "Plan date is required")
    private LocalDate planDate;

    @NotNull(message = "Meal type is required (BREAKFAST, LUNCH, DINNER)")
    private MealType mealType;

    @Min(value = 1, message = "Servings override must be at least 1")
    private Integer servings;
}
