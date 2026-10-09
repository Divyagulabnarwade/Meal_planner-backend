package com.yourname.mealplanner.mealplan;

import com.yourname.mealplanner.common.ResourceNotFoundException;
import com.yourname.mealplanner.mealplan.dto.MealPlanRequest;
import com.yourname.mealplanner.mealplan.dto.MealPlanResponse;
import com.yourname.mealplanner.recipe.Recipe;
import com.yourname.mealplanner.recipe.RecipeRepository;
import com.yourname.mealplanner.user.User;
import com.yourname.mealplanner.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    @Transactional
    public MealPlanResponse assignOrUpdateSlot(MealPlanRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Recipe recipe = recipeRepository.findByIdAndUserId(request.getRecipeId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found or access denied: " + request.getRecipeId()));

        Optional<MealPlanEntry> existing = mealPlanRepository.findByUserIdAndPlanDateAndMealType(
                userId, request.getPlanDate(), request.getMealType());

        MealPlanEntry entry;
        if (existing.isPresent()) {
            entry = existing.get();
            entry.setRecipe(recipe);
            entry.setServings(request.getServings());
        } else {
            entry = MealPlanEntry.builder()
                    .user(user)
                    .recipe(recipe)
                    .planDate(request.getPlanDate())
                    .mealType(request.getMealType())
                    .servings(request.getServings())
                    .build();
        }

        MealPlanEntry saved = mealPlanRepository.save(entry);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MealPlanResponse> getMealPlans(LocalDate startDate, LocalDate endDate, Long userId) {
        List<MealPlanEntry> entries = mealPlanRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        return entries.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteEntry(Long id, Long userId) {
        MealPlanEntry entry = mealPlanRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan entry not found: " + id));
        mealPlanRepository.delete(entry);
    }

    @Transactional
    public void deleteSlot(LocalDate planDate, MealType mealType, Long userId) {
        MealPlanEntry entry = mealPlanRepository.findByUserIdAndPlanDateAndMealType(userId, planDate, mealType)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan entry not found for slot: " + planDate + " " + mealType));
        mealPlanRepository.delete(entry);
    }

    public MealPlanResponse mapToResponse(MealPlanEntry entry) {
        Recipe recipe = entry.getRecipe();
        int effectiveServings = entry.getEffectiveServings();
        int recipeServings = (recipe != null && recipe.getServings() > 0) ? recipe.getServings() : 1;
        double scale = (double) effectiveServings / recipeServings;

        Integer scaledCalories = (recipe != null && recipe.getCalories() != null)
                ? (int) Math.round(recipe.getCalories() * scale) : null;
        java.math.BigDecimal scaledProtein = (recipe != null && recipe.getProteinGrams() != null)
                ? recipe.getProteinGrams().multiply(java.math.BigDecimal.valueOf(scale)).setScale(1, java.math.RoundingMode.HALF_UP) : null;
        java.math.BigDecimal scaledCarbs = (recipe != null && recipe.getCarbsGrams() != null)
                ? recipe.getCarbsGrams().multiply(java.math.BigDecimal.valueOf(scale)).setScale(1, java.math.RoundingMode.HALF_UP) : null;
        java.math.BigDecimal scaledFat = (recipe != null && recipe.getFatGrams() != null)
                ? recipe.getFatGrams().multiply(java.math.BigDecimal.valueOf(scale)).setScale(1, java.math.RoundingMode.HALF_UP) : null;

        return MealPlanResponse.builder()
                .id(entry.getId())
                .recipeId(recipe != null ? recipe.getId() : null)
                .recipeTitle(recipe != null ? recipe.getTitle() : "Unknown")
                .defaultServings(recipe != null ? recipe.getServings() : 1)
                .servings(effectiveServings)
                .planDate(entry.getPlanDate())
                .mealType(entry.getMealType())
                .prepTimeMinutes(recipe != null ? recipe.getPrepTimeMinutes() : null)
                .calories(scaledCalories)
                .proteinGrams(scaledProtein)
                .carbsGrams(scaledCarbs)
                .fatGrams(scaledFat)
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
