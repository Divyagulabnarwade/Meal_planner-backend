package com.yourname.mealplanner.pantry;

import com.yourname.mealplanner.common.ResourceNotFoundException;
import com.yourname.mealplanner.pantry.dto.PantryItemRequest;
import com.yourname.mealplanner.pantry.dto.PantryItemResponse;
import com.yourname.mealplanner.user.User;
import com.yourname.mealplanner.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PantryServiceTest {

    @Mock
    private PantryRepository pantryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PantryService pantryService;

    private User user;
    private PantryItem milkItem;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("chef@example.com").fullName("Chef").build();
        milkItem = PantryItem.builder()
                .id(10L)
                .user(user)
                .name("milk")
                .quantity(new BigDecimal("100"))
                .unit("ml")
                .lowStockThreshold(new BigDecimal("200"))
                .expiryDate(LocalDate.now().plusDays(2))
                .build();
    }

    @Test
    void shouldFlagLowStockAndExpiringSoon() {
        PantryItemResponse response = pantryService.mapToResponse(milkItem);

        assertNotNull(response);
        assertEquals("milk", response.getName());
        assertTrue(response.getIsLowStock());
        assertTrue(response.getIsExpiringSoon());
        assertFalse(response.getIsExpired());
    }

    @Test
    void shouldFlagExpiredItem() {
        PantryItem expired = PantryItem.builder()
                .id(11L)
                .user(user)
                .name("yogurt")
                .quantity(new BigDecimal("200"))
                .unit("g")
                .expiryDate(LocalDate.now().minusDays(1))
                .build();

        PantryItemResponse response = pantryService.mapToResponse(expired);
        assertTrue(response.getIsExpired());
        assertFalse(response.getIsExpiringSoon());
    }

    @Test
    void shouldAddOrUpdateItem() {
        PantryItemRequest req = PantryItemRequest.builder()
                .name("Flour")
                .quantity(new BigDecimal("500"))
                .unit("g")
                .lowStockThreshold(new BigDecimal("100"))
                .expiryDate(LocalDate.now().plusMonths(3))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(pantryRepository.findByUserIdAndNameIgnoreCase(1L, "flour")).thenReturn(Optional.empty());
        when(pantryRepository.save(any(PantryItem.class))).thenAnswer(i -> {
            PantryItem p = i.getArgument(0);
            p.setId(20L);
            return p;
        });

        PantryItemResponse res = pantryService.addOrUpdateItem(req, 1L);
        assertNotNull(res);
        assertEquals("flour", res.getName());
        assertEquals(new BigDecimal("500"), res.getQuantity());
        assertFalse(res.getIsLowStock());
        assertFalse(res.getIsExpired());
    }

    @Test
    void shouldGetExpiringItems() {
        when(pantryRepository.findExpiringItems(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(milkItem));

        List<PantryItemResponse> list = pantryService.getExpiringItems(1L);
        assertEquals(1, list.size());
        assertEquals("milk", list.get(0).getName());
    }

    @Test
    void shouldGetLowStockItems() {
        when(pantryRepository.findLowStockItems(1L))
                .thenReturn(List.of(milkItem));

        List<PantryItemResponse> list = pantryService.getLowStockItems(1L);
        assertEquals(1, list.size());
        assertTrue(list.get(0).getIsLowStock());
    }
}
