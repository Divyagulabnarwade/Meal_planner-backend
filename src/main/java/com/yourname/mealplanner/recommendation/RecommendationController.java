package com.yourname.mealplanner.recommendation;

import com.yourname.mealplanner.common.ApiResponse;
import com.yourname.mealplanner.recommendation.dto.RecommendationRequest;
import com.yourname.mealplanner.recommendation.dto.RecipeRecommendationDto;
import com.yourname.mealplanner.user.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Endpoints for smart rule-based recipe recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    @Operation(summary = "Get recipe recommendations based on pantry availability, dietary tags, and prep time")
    public ResponseEntity<ApiResponse<List<RecipeRecommendationDto>>> getRecommendations(
            @RequestParam(required = false) List<String> dietaryTags,
            @RequestParam(required = false) Integer maxPrepTime,
            @RequestParam(required = false, defaultValue = "true") Boolean matchPantry,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @AuthenticationPrincipal UserPrincipal principal) {

        RecommendationRequest request = RecommendationRequest.builder()
                .dietaryTags(dietaryTags)
                .maxPrepTime(maxPrepTime)
                .matchPantry(matchPantry)
                .limit(limit)
                .build();

        List<RecipeRecommendationDto> recommendations = recommendationService.getRecommendations(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    @PostMapping
    @Operation(summary = "Get recipe recommendations with request body filters")
    public ResponseEntity<ApiResponse<List<RecipeRecommendationDto>>> getRecommendationsWithBody(
            @RequestBody(required = false) RecommendationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (request == null) {
            request = new RecommendationRequest();
        }

        List<RecipeRecommendationDto> recommendations = recommendationService.getRecommendations(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }
}
