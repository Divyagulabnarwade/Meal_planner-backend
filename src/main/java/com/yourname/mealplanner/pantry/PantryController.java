package com.yourname.mealplanner.pantry;

import com.yourname.mealplanner.common.ApiResponse;
import com.yourname.mealplanner.pantry.dto.PantryItemRequest;
import com.yourname.mealplanner.pantry.dto.PantryItemResponse;
import com.yourname.mealplanner.user.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pantry")
@RequiredArgsConstructor
@Tag(name = "Pantry", description = "Endpoints for managing on-hand pantry inventory")
public class PantryController {

    private final PantryService pantryService;

    @GetMapping
    @Operation(summary = "Get all pantry items for the authenticated user")
    public ResponseEntity<ApiResponse<List<PantryItemResponse>>> getPantry(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<PantryItemResponse> items = pantryService.getUserPantry(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get pantry items expiring within 7 days or already expired")
    public ResponseEntity<ApiResponse<List<PantryItemResponse>>> getExpiringItems(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<PantryItemResponse> items = pantryService.getExpiringItems(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get pantry items with quantity at or below low-stock threshold")
    public ResponseEntity<ApiResponse<List<PantryItemResponse>>> getLowStockItems(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<PantryItemResponse> items = pantryService.getLowStockItems(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PostMapping
    @Operation(summary = "Add or update an on-hand pantry item")
    public ResponseEntity<ApiResponse<PantryItemResponse>> addOrUpdateItem(
            @Valid @RequestBody PantryItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PantryItemResponse response = pantryService.addOrUpdateItem(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pantry item saved", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a specific pantry item")
    public ResponseEntity<ApiResponse<PantryItemResponse>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody PantryItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PantryItemResponse response = pantryService.updateItem(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Pantry item updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove an item from pantry")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        pantryService.deleteItem(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
