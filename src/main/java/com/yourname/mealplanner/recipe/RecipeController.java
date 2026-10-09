package com.yourname.mealplanner.recipe;

import com.yourname.mealplanner.common.ApiResponse;
import com.yourname.mealplanner.common.PageResponse;
import com.yourname.mealplanner.recipe.dto.RecipeRequest;
import com.yourname.mealplanner.recipe.dto.RecipeResponse;
import com.yourname.mealplanner.recipe.dto.RecipeSummaryDto;
import com.yourname.mealplanner.user.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
@Tag(name = "Recipes", description = "Endpoints for managing user recipes and ingredients")
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping
    @Operation(summary = "Create a new recipe with ingredients and tags")
    public ResponseEntity<ApiResponse<RecipeResponse>> createRecipe(
            @Valid @RequestBody RecipeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        RecipeResponse response = recipeService.createRecipe(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recipe created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get paginated recipes with optional search and tag filters")
    public ResponseEntity<ApiResponse<PageResponse<RecipeSummaryDto>>> getRecipes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tag,
            @PageableDefault(size = 12, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        PageResponse<RecipeSummaryDto> recipes = recipeService.getRecipes(principal.getId(), search, tag, pageable);
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recipe details by ID")
    public ResponseEntity<ApiResponse<RecipeResponse>> getRecipeById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        RecipeResponse response = recipeService.getRecipeById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing recipe")
    public ResponseEntity<ApiResponse<RecipeResponse>> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        RecipeResponse response = recipeService.updateRecipe(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Recipe updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a recipe by ID")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        recipeService.deleteRecipe(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all distinct tags used across the current user's recipes")
    public ResponseEntity<ApiResponse<List<String>>> getUserTags(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<String> tags = recipeService.getUserTags(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(tags));
    }
}
