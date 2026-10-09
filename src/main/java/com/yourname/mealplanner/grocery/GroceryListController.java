package com.yourname.mealplanner.grocery;

import com.yourname.mealplanner.common.ApiResponse;
import com.yourname.mealplanner.grocery.dto.*;
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
@RequestMapping("/api/grocery-lists")
@RequiredArgsConstructor
@Tag(name = "Grocery Lists", description = "Endpoints for generating, viewing, and managing grocery shopping lists")
public class GroceryListController {

    private final GroceryListService groceryListService;

    @PostMapping("/generate")
    @Operation(summary = "Generate a consolidated grocery list from planned meals in a date range")
    public ResponseEntity<ApiResponse<GroceryListResponse>> generateGroceryList(
            @RequestBody(required = false) GenerateGroceryListRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (request == null) {
            request = new GenerateGroceryListRequest();
        }
        GroceryListResponse response = groceryListService.generateGroceryList(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Grocery list generated successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all grocery lists for the authenticated user")
    public ResponseEntity<ApiResponse<List<GroceryListResponse>>> getUserGroceryLists(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<GroceryListResponse> lists = groceryListService.getUserGroceryLists(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(lists));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific grocery list by ID with all items")
    public ResponseEntity<ApiResponse<GroceryListResponse>> getGroceryListById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        GroceryListResponse response = groceryListService.getGroceryListById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/items/{itemId}")
    @Operation(summary = "Check or uncheck an item in a grocery list")
    public ResponseEntity<ApiResponse<GroceryItemResponse>> toggleItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @Valid @RequestBody ToggleItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        GroceryItemResponse response = groceryListService.toggleItem(id, itemId, request.getChecked(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/items/{itemId}/quantity")
    @Operation(summary = "Update quantity of an item in a grocery list")
    public ResponseEntity<ApiResponse<GroceryItemResponse>> updateItemQuantity(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemQuantityRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        GroceryItemResponse response = groceryListService.updateItemQuantity(id, itemId, request.getQuantity(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Grocery item quantity updated", response));
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Add a custom item to a grocery list")
    public ResponseEntity<ApiResponse<GroceryItemResponse>> addCustomItem(
            @PathVariable Long id,
            @Valid @RequestBody AddCustomItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        GroceryItemResponse response = groceryListService.addCustomItem(id, request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Custom item added", response));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(summary = "Delete an item from a grocery list")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserPrincipal principal) {
        groceryListService.deleteItem(id, itemId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an entire grocery list")
    public ResponseEntity<Void> deleteList(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        groceryListService.deleteList(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
