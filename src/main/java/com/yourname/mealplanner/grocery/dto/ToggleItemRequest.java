package com.yourname.mealplanner.grocery.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToggleItemRequest {

    @NotNull(message = "Checked status is required")
    private Boolean checked;
}
