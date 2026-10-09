package com.yourname.mealplanner.dashboard.dto;

import com.yourname.mealplanner.mealplan.dto.MealPlanResponse;
import com.yourname.mealplanner.recipe.dto.RecipeSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private long totalRecipes;
    private long plannedMealsThisWeek;
    private long uncheckedGroceryItems;
    private long totalPantryItems;
    private long expiringPantryItems;
    private long lowStockPantryItems;

    @Builder.Default
    private List<RecipeSummaryDto> recentRecipes = new ArrayList<>();

    @Builder.Default
    private List<MealPlanResponse> thisWeekMeals = new ArrayList<>();
}
