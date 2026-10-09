package com.yourname.mealplanner.recipe.dto;

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
public class RecipeSummaryDto {

    private Long id;
    private String title;
    private String description;
    private Integer servings;
    private Integer prepTimeMinutes;
    private Integer calories;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private int ingredientCount;
}
