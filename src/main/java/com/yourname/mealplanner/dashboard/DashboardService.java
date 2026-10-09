package com.yourname.mealplanner.dashboard;

import com.yourname.mealplanner.dashboard.dto.DashboardSummaryResponse;
import com.yourname.mealplanner.grocery.GroceryListRepository;
import com.yourname.mealplanner.mealplan.MealPlanEntry;
import com.yourname.mealplanner.mealplan.MealPlanRepository;
import com.yourname.mealplanner.mealplan.MealPlanService;
import com.yourname.mealplanner.mealplan.dto.MealPlanResponse;
import com.yourname.mealplanner.pantry.PantryRepository;
import com.yourname.mealplanner.recipe.Recipe;
import com.yourname.mealplanner.recipe.RecipeRepository;
import com.yourname.mealplanner.recipe.RecipeService;
import com.yourname.mealplanner.recipe.dto.RecipeSummaryDto;
import com.yourname.mealplanner.user.User;
import com.yourname.mealplanner.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RecipeRepository recipeRepository;
    private final MealPlanRepository mealPlanRepository;
    private final GroceryListRepository groceryListRepository;
    private final PantryRepository pantryRepository;
    private final UserRepository userRepository;
    private final RecipeService recipeService;
    private final MealPlanService mealPlanService;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        long totalRecipes = recipeRepository.countByUserId(userId);
        long plannedMeals = mealPlanRepository.countByUserIdAndPlanDateBetween(userId, startOfWeek, endOfWeek);
        long uncheckedGroceries = groceryListRepository.countUncheckedItemsByUserId(userId);
        long totalPantry = pantryRepository.countByUserId(userId);
        long expiringPantry = pantryRepository.countExpiringItems(userId, today.plusDays(7));
        long lowStockPantry = pantryRepository.countLowStockItems(userId);

        User user = userRepository.getReferenceById(userId);
        Page<Recipe> recentRecipesPage = recipeRepository.findByUser(
                user, PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<RecipeSummaryDto> recentRecipes = recentRecipesPage.getContent().stream()
                .map(recipeService::mapToSummary)
                .collect(Collectors.toList());

        List<MealPlanEntry> weekEntries = mealPlanRepository.findByUserIdAndDateRange(userId, startOfWeek, endOfWeek);
        List<MealPlanResponse> thisWeekMeals = weekEntries.stream()
                .map(mealPlanService::mapToResponse)
                .collect(Collectors.toList());

        return DashboardSummaryResponse.builder()
                .totalRecipes(totalRecipes)
                .plannedMealsThisWeek(plannedMeals)
                .uncheckedGroceryItems(uncheckedGroceries)
                .totalPantryItems(totalPantry)
                .expiringPantryItems(expiringPantry)
                .lowStockPantryItems(lowStockPantry)
                .recentRecipes(recentRecipes)
                .thisWeekMeals(thisWeekMeals)
                .build();
    }
}
