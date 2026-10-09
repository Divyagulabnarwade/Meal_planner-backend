package com.yourname.mealplanner.recommendation;

import com.yourname.mealplanner.pantry.PantryItem;
import com.yourname.mealplanner.recipe.Recipe;
import com.yourname.mealplanner.recipe.RecipeIngredient;
import com.yourname.mealplanner.recommendation.dto.RecommendationRequest;
import com.yourname.mealplanner.recommendation.dto.RecipeRecommendationDto;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Deterministic, rule-based recommendation engine.
 * Computes transparent heuristic match scores across pantry ingredient availability,
 * dietary constraints, and prep/cook duration preferences.
 */
@Component
public class RuleBasedRecommendationEngine implements RecipeRecommendationEngine {

    @Override
    public List<RecipeRecommendationDto> recommend(
            List<Recipe> candidateRecipes,
            List<PantryItem> pantryItems,
            RecommendationRequest request) {

        if (candidateRecipes == null || candidateRecipes.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> pantryNames = pantryItems != null
                ? pantryItems.stream()
                .map(p -> p.getName().trim().toLowerCase())
                .collect(Collectors.toSet())
                : Collections.emptySet();

        List<String> requestedTags = request.getDietaryTags() != null
                ? request.getDietaryTags().stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toList())
                : Collections.emptyList();

        List<RecipeRecommendationDto> results = new ArrayList<>();

        for (Recipe recipe : candidateRecipes) {
            List<String> matchedIngredients = new ArrayList<>();
            List<String> missingIngredients = new ArrayList<>();

            List<RecipeIngredient> ingredients = recipe.getIngredients() != null
                    ? recipe.getIngredients()
                    : Collections.emptyList();

            for (RecipeIngredient ing : ingredients) {
                String ingName = ing.getName().trim().toLowerCase();
                boolean inPantry = isIngredientInPantry(ingName, pantryNames);
                if (inPantry) {
                    matchedIngredients.add(ing.getName());
                } else {
                    missingIngredients.add(ing.getName());
                }
            }

            // 1. Pantry Coverage Score (Max 60 pts)
            double pantryScore = 0.0;
            boolean matchPantry = request.getMatchPantry() == null || request.getMatchPantry();
            if (matchPantry) {
                if (!ingredients.isEmpty()) {
                    double pantryRatio = (double) matchedIngredients.size() / ingredients.size();
                    pantryScore = pantryRatio * 60.0;
                } else {
                    pantryScore = 30.0;
                }
            } else {
                pantryScore = 40.0; // neutral weight when pantry match is unrequested
            }

            // 2. Dietary Alignment Score (Max 25 pts)
            double dietaryScore = 25.0;
            List<String> recipeTags = recipe.getDietaryTagList() != null
                    ? recipe.getDietaryTagList().stream().map(String::toLowerCase).collect(Collectors.toList())
                    : Collections.emptyList();

            if (!requestedTags.isEmpty()) {
                long matchedTagCount = requestedTags.stream()
                        .filter(reqTag -> recipeTags.stream().anyMatch(rt -> rt.equalsIgnoreCase(reqTag)))
                        .count();
                dietaryScore = ((double) matchedTagCount / requestedTags.size()) * 25.0;
            }

            // 3. Time Convenience Score (Max 15 pts)
            int totalTime = recipe.getPrepTimeMinutes() != null ? recipe.getPrepTimeMinutes() : 0;

            double timeScore = 15.0;
            if (request.getMaxPrepTime() != null && request.getMaxPrepTime() > 0) {
                if (totalTime <= request.getMaxPrepTime()) {
                    timeScore = 15.0;
                } else {
                    int diff = totalTime - request.getMaxPrepTime();
                    timeScore = Math.max(0.0, 15.0 - (diff * 0.5));
                }
            } else {
                if (totalTime > 60) {
                    timeScore = Math.max(5.0, 15.0 - ((totalTime - 60) * 0.2));
                }
            }

            double totalScore = Math.min(100.0, Math.max(0.0, pantryScore + dietaryScore + timeScore));
            double roundedScore = Math.round(totalScore * 10.0) / 10.0;

            String explanation = buildExplanation(
                    matchedIngredients.size(),
                    ingredients.size(),
                    matchPantry,
                    recipe.getDietaryTagList(),
                    requestedTags,
                    totalTime,
                    request.getMaxPrepTime()
            );

            results.add(RecipeRecommendationDto.builder()
                    .id(recipe.getId())
                    .title(recipe.getTitle())
                    .description(recipe.getDescription())
                    .prepTimeMinutes(recipe.getPrepTimeMinutes())
                    .cookTimeMinutes(null)
                    .totalTimeMinutes(totalTime)
                    .servings(recipe.getServings())
                    .dietaryTags(recipe.getDietaryTagList())
                    .calories(recipe.getCalories())
                    .proteinGrams(recipe.getProteinGrams())
                    .carbsGrams(recipe.getCarbsGrams())
                    .fatGrams(recipe.getFatGrams())
                    .matchScore(roundedScore)
                    .matchedIngredients(matchedIngredients)
                    .missingIngredients(missingIngredients)
                    .explanation(explanation)
                    .build());
        }

        int limit = (request.getLimit() != null && request.getLimit() > 0) ? request.getLimit() : 10;

        return results.stream()
                .sorted(Comparator.comparingDouble(RecipeRecommendationDto::getMatchScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private boolean isIngredientInPantry(String ingredientName, Set<String> pantryNames) {
        if (pantryNames.isEmpty()) {
            return false;
        }
        for (String pantry : pantryNames) {
            if (ingredientName.equals(pantry)
                    || ingredientName.contains(pantry)
                    || pantry.contains(ingredientName)) {
                return true;
            }
        }
        return false;
    }

    private String buildExplanation(
            int matchedIngCount,
            int totalIngCount,
            boolean matchPantry,
            List<String> recipeTags,
            List<String> requestedTags,
            int totalTime,
            Integer maxPrepTime) {

        List<String> parts = new ArrayList<>();

        if (matchPantry && totalIngCount > 0) {
            int pct = (int) Math.round(((double) matchedIngCount / totalIngCount) * 100);
            parts.add(String.format("Pantry match: %d of %d ingredients (%d%%)", matchedIngCount, totalIngCount, pct));
        }

        if (!requestedTags.isEmpty()) {
            parts.add("Dietary constraints matched");
        } else if (recipeTags != null && !recipeTags.isEmpty()) {
            parts.add("Tags: " + String.join(", ", recipeTags));
        }

        if (maxPrepTime != null && maxPrepTime > 0) {
            if (totalTime <= maxPrepTime) {
                parts.add(String.format("Quick prep: %d mins (under your %d min limit)", totalTime, maxPrepTime));
            } else {
                parts.add(String.format("Takes %d mins", totalTime));
            }
        } else if (totalTime > 0) {
            parts.add(String.format("Ready in %d mins", totalTime));
        }

        return String.join(" • ", parts);
    }
}
