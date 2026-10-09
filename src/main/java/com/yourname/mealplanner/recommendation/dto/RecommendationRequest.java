package com.yourname.mealplanner.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {

    private List<String> dietaryTags;
    private Integer maxPrepTime;
    @Builder.Default
    private Boolean matchPantry = true;
    @Builder.Default
    private Integer limit = 10;
}
