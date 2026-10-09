package com.yourname.mealplanner.recommendation;

import com.yourname.mealplanner.common.ResourceNotFoundException;
import com.yourname.mealplanner.pantry.PantryItem;
import com.yourname.mealplanner.pantry.PantryRepository;
import com.yourname.mealplanner.recipe.Recipe;
import com.yourname.mealplanner.recipe.RecipeRepository;
import com.yourname.mealplanner.recommendation.dto.RecommendationRequest;
import com.yourname.mealplanner.recommendation.dto.RecipeRecommendationDto;
import com.yourname.mealplanner.user.User;
import com.yourname.mealplanner.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecipeRepository recipeRepository;
    private final PantryRepository pantryRepository;
    private final UserRepository userRepository;
    private final RecipeRecommendationEngine recommendationEngine;

    @Transactional(readOnly = true)
    public List<RecipeRecommendationDto> getRecommendations(Long userId, RecommendationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        List<Recipe> candidateRecipes = recipeRepository.findByUser(user);
        List<PantryItem> pantryItems = pantryRepository.findByUserIdOrderByNameAsc(userId);

        return recommendationEngine.recommend(candidateRecipes, pantryItems, request);
    }
}
