package com.yourname.mealplanner.grocery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateGroceryListRequest {

    private String title;

    private LocalDate startDate;

    private LocalDate endDate;

    @Builder.Default
    private boolean subtractPantry = true;
}
