package com.yourname.mealplanner.grocery;

import com.yourname.mealplanner.common.ResourceNotFoundException;
import com.yourname.mealplanner.grocery.dto.*;
import com.yourname.mealplanner.mealplan.MealPlanEntry;
import com.yourname.mealplanner.mealplan.MealPlanRepository;
import com.yourname.mealplanner.pantry.PantryItem;
import com.yourname.mealplanner.pantry.PantryRepository;
import com.yourname.mealplanner.user.User;
import com.yourname.mealplanner.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroceryListService {

    private final GroceryListRepository groceryListRepository;
    private final GroceryItemRepository groceryItemRepository;
    private final MealPlanRepository mealPlanRepository;
    private final PantryRepository pantryRepository;
    private final UserRepository userRepository;
    private final GroceryMergeService groceryMergeService;

    @Transactional
    public GroceryListResponse generateGroceryList(GenerateGroceryListRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        // Default to current week (Monday to Sunday) if not provided
        if (startDate == null || endDate == null) {
            LocalDate today = LocalDate.now();
            startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            endDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        }

        List<MealPlanEntry> entries = mealPlanRepository.findByUserIdAndDateRangeWithRecipeAndIngredients(
                userId, startDate, endDate);

        List<PantryItem> pantryItems = null;
        if (request.isSubtractPantry()) {
            pantryItems = pantryRepository.findByUserIdOrderByNameAsc(userId);
        }

        List<GroceryItem> mergedItems = groceryMergeService.generateMergedList(
                entries, pantryItems, request.isSubtractPantry());

        String title = StringUtils.hasText(request.getTitle())
                ? request.getTitle().trim()
                : "Groceries (" + startDate + " to " + endDate + ")";

        GroceryList groceryList = GroceryList.builder()
                .user(user)
                .title(title)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        for (GroceryItem item : mergedItems) {
            groceryList.addItem(item);
        }

        GroceryList saved = groceryListRepository.save(groceryList);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public GroceryListResponse getGroceryListById(Long id, Long userId) {
        GroceryList list = groceryListRepository.findByIdAndUserIdWithItems(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Grocery list not found: " + id));
        return mapToResponse(list);
    }

    @Transactional(readOnly = true)
    public List<GroceryListResponse> getUserGroceryLists(Long userId) {
        List<GroceryList> lists = groceryListRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return lists.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public GroceryItemResponse toggleItem(Long listId, Long itemId, Boolean checked, Long userId) {
        // Verify list ownership
        groceryListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Grocery list not found: " + listId));

        GroceryItem item = groceryItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Grocery item not found: " + itemId));

        item.setChecked(checked);
        GroceryItem updated = groceryItemRepository.save(item);
        return mapItemToResponse(updated);
    }

    @Transactional
    public GroceryItemResponse updateItemQuantity(Long listId, Long itemId, java.math.BigDecimal quantity, Long userId) {
        groceryListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Grocery list not found: " + listId));

        GroceryItem item = groceryItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Grocery item not found: " + itemId));

        item.setQuantity(quantity);
        GroceryItem updated = groceryItemRepository.save(item);
        return mapItemToResponse(updated);
    }

    @Transactional
    public GroceryItemResponse addCustomItem(Long listId, AddCustomItemRequest request, Long userId) {
        GroceryList list = groceryListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Grocery list not found: " + listId));

        GroceryItem item = GroceryItem.builder()
                .name(request.getName().trim().toLowerCase())
                .quantity(request.getQuantity())
                .unit(request.getUnit().trim())
                .checked(false)
                .isCustom(true)
                .build();

        list.addItem(item);
        GroceryItem saved = groceryItemRepository.save(item);
        return mapItemToResponse(saved);
    }

    @Transactional
    public void deleteItem(Long listId, Long itemId, Long userId) {
        groceryListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Grocery list not found: " + listId));

        GroceryItem item = groceryItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Grocery item not found: " + itemId));

        groceryItemRepository.delete(item);
    }

    @Transactional
    public void deleteList(Long id, Long userId) {
        GroceryList list = groceryListRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Grocery list not found: " + id));
        groceryListRepository.delete(list);
    }

    public GroceryListResponse mapToResponse(GroceryList list) {
        List<GroceryItemResponse> itemResponses = list.getItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        int total = itemResponses.size();
        int checkedCount = (int) itemResponses.stream().filter(GroceryItemResponse::getChecked).count();

        return GroceryListResponse.builder()
                .id(list.getId())
                .title(list.getTitle())
                .startDate(list.getStartDate())
                .endDate(list.getEndDate())
                .items(itemResponses)
                .totalItems(total)
                .checkedItems(checkedCount)
                .createdAt(list.getCreatedAt())
                .build();
    }

    public GroceryItemResponse mapItemToResponse(GroceryItem item) {
        return GroceryItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .checked(item.getChecked())
                .isCustom(item.getIsCustom())
                .build();
    }
}
