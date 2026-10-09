package com.yourname.mealplanner.pantry;

import com.yourname.mealplanner.common.ResourceNotFoundException;
import com.yourname.mealplanner.pantry.dto.PantryItemRequest;
import com.yourname.mealplanner.pantry.dto.PantryItemResponse;
import com.yourname.mealplanner.user.User;
import com.yourname.mealplanner.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PantryService {

    private final PantryRepository pantryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PantryItemResponse> getUserPantry(Long userId) {
        List<PantryItem> items = pantryRepository.findByUserIdOrderByNameAsc(userId);
        return items.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PantryItemResponse> getExpiringItems(Long userId) {
        LocalDate cutoff = LocalDate.now().plusDays(7);
        List<PantryItem> items = pantryRepository.findExpiringItems(userId, cutoff);
        return items.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PantryItemResponse> getLowStockItems(Long userId) {
        List<PantryItem> items = pantryRepository.findLowStockItems(userId);
        return items.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public PantryItemResponse addOrUpdateItem(PantryItemRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String name = request.getName().trim().toLowerCase();
        Optional<PantryItem> existing = pantryRepository.findByUserIdAndNameIgnoreCase(userId, name);

        PantryItem item;
        if (existing.isPresent()) {
            item = existing.get();
            item.setQuantity(request.getQuantity());
            item.setUnit(request.getUnit().trim());
            item.setExpiryDate(request.getExpiryDate());
            item.setLowStockThreshold(request.getLowStockThreshold());
        } else {
            item = PantryItem.builder()
                    .user(user)
                    .name(name)
                    .quantity(request.getQuantity())
                    .unit(request.getUnit().trim())
                    .expiryDate(request.getExpiryDate())
                    .lowStockThreshold(request.getLowStockThreshold())
                    .build();
        }

        PantryItem saved = pantryRepository.save(item);
        return mapToResponse(saved);
    }

    @Transactional
    public PantryItemResponse updateItem(Long id, PantryItemRequest request, Long userId) {
        PantryItem item = pantryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Pantry item not found: " + id));

        item.setName(request.getName().trim().toLowerCase());
        item.setQuantity(request.getQuantity());
        item.setUnit(request.getUnit().trim());
        item.setExpiryDate(request.getExpiryDate());
        item.setLowStockThreshold(request.getLowStockThreshold());

        PantryItem saved = pantryRepository.save(item);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteItem(Long id, Long userId) {
        PantryItem item = pantryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Pantry item not found: " + id));
        pantryRepository.delete(item);
    }

    public PantryItemResponse mapToResponse(PantryItem item) {
        LocalDate today = LocalDate.now();
        boolean isExpired = item.getExpiryDate() != null && item.getExpiryDate().isBefore(today);
        boolean isExpiringSoon = item.getExpiryDate() != null 
                && !item.getExpiryDate().isBefore(today) 
                && !item.getExpiryDate().isAfter(today.plusDays(7));
        boolean isLowStock = item.getLowStockThreshold() != null 
                && item.getQuantity() != null 
                && item.getQuantity().compareTo(item.getLowStockThreshold()) <= 0;

        return PantryItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .expiryDate(item.getExpiryDate())
                .lowStockThreshold(item.getLowStockThreshold())
                .isExpired(isExpired)
                .isExpiringSoon(isExpiringSoon)
                .isLowStock(isLowStock)
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
