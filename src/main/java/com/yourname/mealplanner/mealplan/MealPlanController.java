package com.yourname.mealplanner.mealplan;

import com.yourname.mealplanner.common.ApiResponse;
import com.yourname.mealplanner.mealplan.dto.MealPlanRequest;
import com.yourname.mealplanner.mealplan.dto.MealPlanResponse;
import com.yourname.mealplanner.user.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/meal-plans")
@RequiredArgsConstructor
@Tag(name = "Meal Plans", description = "Endpoints for weekly meal planning and scheduling")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @GetMapping
    @Operation(summary = "Get meal plans within a date range (e.g. current week)")
    public ResponseEntity<ApiResponse<List<MealPlanResponse>>> getMealPlans(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<MealPlanResponse> plans = mealPlanService.getMealPlans(startDate, endDate, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    @PostMapping
    @Operation(summary = "Assign or update a recipe in a specific date and meal slot")
    public ResponseEntity<ApiResponse<MealPlanResponse>> assignMealSlot(
            @Valid @RequestBody MealPlanRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        MealPlanResponse response = mealPlanService.assignOrUpdateSlot(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Meal slot assigned successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a meal plan entry by ID")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        mealPlanService.deleteEntry(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/slot")
    @Operation(summary = "Remove a meal slot by date and meal type")
    public ResponseEntity<Void> deleteSlot(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate,
            @RequestParam MealType mealType,
            @AuthenticationPrincipal UserPrincipal principal) {
        mealPlanService.deleteSlot(planDate, mealType, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
