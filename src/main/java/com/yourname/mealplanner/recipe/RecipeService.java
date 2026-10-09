package com.yourname.mealplanner.recipe;

import com.yourname.mealplanner.common.PageResponse;
import com.yourname.mealplanner.common.ResourceNotFoundException;
import com.yourname.mealplanner.recipe.dto.RecipeIngredientDto;
import com.yourname.mealplanner.recipe.dto.RecipeRequest;
import com.yourname.mealplanner.recipe.dto.RecipeResponse;
import com.yourname.mealplanner.recipe.dto.RecipeSummaryDto;
import com.yourname.mealplanner.user.User;
import com.yourname.mealplanner.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    @Transactional
    public RecipeResponse createRecipe(RecipeRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Recipe recipe = Recipe.builder()
                .user(user)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .servings(request.getServings())
                .prepTimeMinutes(request.getPrepTimeMinutes())
                .instructions(request.getInstructions())
                .calories(request.getCalories())
                .proteinGrams(request.getProteinGrams())
                .carbsGrams(request.getCarbsGrams())
                .fatGrams(request.getFatGrams())
                .build();

        if (request.getIngredients() != null) {
            for (RecipeIngredientDto ingDto : request.getIngredients()) {
                RecipeIngredient ingredient = RecipeIngredient.builder()
                        .name(ingDto.getName().trim())
                        .quantity(ingDto.getQuantity())
                        .unit(ingDto.getUnit().trim())
                        .build();
                recipe.addIngredient(ingredient);
            }
        }

        if (request.getTags() != null) {
            for (String tagName : request.getTags()) {
                if (StringUtils.hasText(tagName)) {
                    RecipeTag tag = RecipeTag.builder()
                            .tag(tagName.trim().toLowerCase())
                            .build();
                    recipe.addTag(tag);
                }
            }
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        return mapToResponse(savedRecipe);
    }

    @Transactional(readOnly = true)
    public RecipeResponse getRecipeById(Long id, Long userId) {
        Recipe recipe = recipeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));
        return mapToResponse(recipe);
    }

    @Transactional(readOnly = true)
    public PageResponse<RecipeSummaryDto> getRecipes(Long userId, String search, String tag, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Page<Recipe> page;
        boolean hasSearch = StringUtils.hasText(search);
        boolean hasTag = StringUtils.hasText(tag);

        if (hasSearch && hasTag) {
            page = recipeRepository.findDistinctByUserAndTitleContainingIgnoreCaseAndTagsTagIgnoreCase(
                    user, search.trim(), tag.trim().toLowerCase(), pageable);
        } else if (hasSearch) {
            page = recipeRepository.findByUserAndTitleContainingIgnoreCase(user, search.trim(), pageable);
        } else if (hasTag) {
            page = recipeRepository.findDistinctByUserAndTagsTagIgnoreCase(user, tag.trim().toLowerCase(), pageable);
        } else {
            page = recipeRepository.findByUser(user, pageable);
        }

        Page<RecipeSummaryDto> dtoPage = page.map(this::mapToSummary);
        return PageResponse.from(dtoPage);
    }

    @Transactional
    public RecipeResponse updateRecipe(Long id, RecipeRequest request, Long userId) {
        Recipe recipe = recipeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));

        recipe.setTitle(request.getTitle().trim());
        recipe.setDescription(request.getDescription());
        recipe.setServings(request.getServings());
        recipe.setPrepTimeMinutes(request.getPrepTimeMinutes());
        recipe.setInstructions(request.getInstructions());
        recipe.setCalories(request.getCalories());
        recipe.setProteinGrams(request.getProteinGrams());
        recipe.setCarbsGrams(request.getCarbsGrams());
        recipe.setFatGrams(request.getFatGrams());

        // Update ingredients: clear and re-populate
        recipe.clearIngredients();
        if (request.getIngredients() != null) {
            for (RecipeIngredientDto ingDto : request.getIngredients()) {
                RecipeIngredient ingredient = RecipeIngredient.builder()
                        .name(ingDto.getName().trim())
                        .quantity(ingDto.getQuantity())
                        .unit(ingDto.getUnit().trim())
                        .build();
                recipe.addIngredient(ingredient);
            }
        }

        // Update tags: clear and re-populate
        recipe.clearTags();
        if (request.getTags() != null) {
            for (String tagName : request.getTags()) {
                if (StringUtils.hasText(tagName)) {
                    RecipeTag tag = RecipeTag.builder()
                            .tag(tagName.trim().toLowerCase())
                            .build();
                    recipe.addTag(tag);
                }
            }
        }

        Recipe updated = recipeRepository.save(recipe);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteRecipe(Long id, Long userId) {
        Recipe recipe = recipeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));
        recipeRepository.delete(recipe);
    }

    @Transactional(readOnly = true)
    public List<String> getUserTags(Long userId) {
        return recipeRepository.findAllTagsByUserId(userId);
    }

    public RecipeResponse mapToResponse(Recipe recipe) {
        List<String> tags = recipe.getTags().stream()
                .map(RecipeTag::getTag)
                .sorted()
                .collect(Collectors.toList());

        List<RecipeIngredientDto> ingredients = recipe.getIngredients().stream()
                .map(ing -> RecipeIngredientDto.builder()
                        .id(ing.getId())
                        .name(ing.getName())
                        .quantity(ing.getQuantity())
                        .unit(ing.getUnit())
                        .build())
                .collect(Collectors.toList());

        return RecipeResponse.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .servings(recipe.getServings())
                .prepTimeMinutes(recipe.getPrepTimeMinutes())
                .instructions(recipe.getInstructions())
                .calories(recipe.getCalories())
                .proteinGrams(recipe.getProteinGrams())
                .carbsGrams(recipe.getCarbsGrams())
                .fatGrams(recipe.getFatGrams())
                .tags(tags)
                .ingredients(ingredients)
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .build();
    }

    public RecipeSummaryDto mapToSummary(Recipe recipe) {
        List<String> tags = recipe.getTags().stream()
                .map(RecipeTag::getTag)
                .sorted()
                .collect(Collectors.toList());

        return RecipeSummaryDto.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .servings(recipe.getServings())
                .prepTimeMinutes(recipe.getPrepTimeMinutes())
                .calories(recipe.getCalories())
                .tags(tags)
                .ingredientCount(recipe.getIngredients().size())
                .build();
    }
}
