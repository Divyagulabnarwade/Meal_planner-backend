package com.yourname.mealplanner.pantry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PantryItemResponse {

    private Long id;
    private String name;
    private BigDecimal quantity;
    private String unit;
    private LocalDate expiryDate;
    private BigDecimal lowStockThreshold;
    private Boolean isExpired;
    private Boolean isExpiringSoon;
    private Boolean isLowStock;
    private LocalDateTime updatedAt;
}
