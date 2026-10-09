package com.yourname.mealplanner.grocery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroceryItemResponse {

    private Long id;
    private String name;
    private BigDecimal quantity;
    private String unit;
    private Boolean checked;
    private Boolean isCustom;
}
