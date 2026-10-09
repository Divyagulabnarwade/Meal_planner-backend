package com.yourname.mealplanner.mealplan;

import com.yourname.mealplanner.common.ResourceNotFoundException;
import com.yourname.mealplanner.mealplan.dto.MealPlanRequest;
import com.yourname.mealplanner.mealplan.dto.MealPlanResponse;
import com.yourname.mealplanner.recipe.Recipe;
import com.yourname.mealplanner.recipe.RecipeRepository;
import com.yourname.mealplanner.user.User;
import com.yourname.mealplanner.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    @Mock
    private MealPlanRepository mealPlanRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MealPlanService mealPlanService;

    private User user;
    private Recipe recipe;
    private MealPlanEntry entry;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@example.com").fullName("User").build();
        recipe = Recipe.builder().id(10L).user(user).title("Pancakes").servings(2).build();
        entry = MealPlanEntry.builder()
                .id(100L)
                .user(user)
                .recipe(recipe)
                .planDate(LocalDate.of(2026, 9, 21))
                .mealType(MealType.BREAKFAST)
                .servings(4)
                .build();
    }

    @Test
    void shouldAssignNewSlotSuccessfully() {
        MealPlanRequest request = MealPlanRequest.builder()
                .recipeId(10L)
                .planDate(LocalDate.of(2026, 9, 21))
                .mealType(MealType.BREAKFAST)
                .servings(4)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(recipeRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(recipe));
        when(mealPlanRepository.findByUserIdAndPlanDateAndMealType(1L, request.getPlanDate(), request.getMealType()))
                .thenReturn(Optional.empty());
        when(mealPlanRepository.save(any(MealPlanEntry.class))).thenReturn(entry);

        MealPlanResponse response = mealPlanService.assignOrUpdateSlot(request, 1L);

        assertNotNull(response);
        assertEquals(10L, response.getRecipeId());
        assertEquals("Pancakes", response.getRecipeTitle());
        assertEquals(4, response.getServings());
        assertEquals(MealType.BREAKFAST, response.getMealType());
    }

    @Test
    void shouldPreventAssigningRecipeOwnedByAnotherUser() {
        MealPlanRequest request = MealPlanRequest.builder()
                .recipeId(999L)
                .planDate(LocalDate.of(2026, 9, 21))
                .mealType(MealType.LUNCH)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(recipeRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            mealPlanService.assignOrUpdateSlot(request, 1L);
        });
    }

    @Test
    void shouldGetMealPlansForDateRange() {
        LocalDate start = LocalDate.of(2026, 9, 21);
        LocalDate end = LocalDate.of(2026, 9, 27);

        when(mealPlanRepository.findByUserIdAndDateRange(1L, start, end))
                .thenReturn(List.of(entry));

        List<MealPlanResponse> results = mealPlanService.getMealPlans(start, end, 1L);

        assertEquals(1, results.size());
        assertEquals("Pancakes", results.get(0).getRecipeTitle());
    }

    @Test
    void shouldDeleteSlotSuccessfully() {
        LocalDate date = LocalDate.of(2026, 9, 21);
        when(mealPlanRepository.findByUserIdAndPlanDateAndMealType(1L, date, MealType.BREAKFAST))
                .thenReturn(Optional.of(entry));

        mealPlanService.deleteSlot(date, MealType.BREAKFAST, 1L);

        verify(mealPlanRepository).delete(entry);
    }
}
