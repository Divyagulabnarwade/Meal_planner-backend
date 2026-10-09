package com.yourname.mealplanner.recommendation;

import com.yourname.mealplanner.pantry.PantryItem;
import com.yourname.mealplanner.recipe.Recipe;
import com.yourname.mealplanner.recommendation.dto.RecommendationRequest;
import com.yourname.mealplanner.recommendation.dto.RecipeRecommendationDto;

import java.util.List;

/**
 * Strategy interface for recipe recommendation engines.
 * Enables clean separation of concern and future drop-in ML/AI or hybrid algorithms.
 */
public interface RecipeRecommendationEngine {

    List<RecipeRecommendationDto> recommend(
            List<Recipe> candidateRecipes,
            List<PantryItem> pantryItems,
            RecommendationRequest request
    );
}
