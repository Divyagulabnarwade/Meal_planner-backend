package com.yourname.mealplanner.grocery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroceryListResponse {

    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;

    @Builder.Default
    private List<GroceryItemResponse> items = new ArrayList<>();

    private int totalItems;
    private int checkedItems;
    private LocalDateTime createdAt;
}
