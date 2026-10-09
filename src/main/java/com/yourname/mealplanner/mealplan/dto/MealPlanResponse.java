package com.yourname.mealplanner.mealplan.dto;

import com.yourname.mealplanner.mealplan.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanResponse {

    private Long id;
    private Long recipeId;
    private String recipeTitle;
    private Integer defaultServings;
    private Integer servings;
    private LocalDate planDate;
    private MealType mealType;
    private Integer prepTimeMinutes;

    private Integer calories;
    private BigDecimal proteinGrams;
    private BigDecimal carbsGrams;
    private BigDecimal fatGrams;

    private LocalDateTime createdAt;
}
