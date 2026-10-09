package com.yourname.mealplanner.recipe;

import com.yourname.mealplanner.common.ResourceNotFoundException;
import com.yourname.mealplanner.recipe.dto.RecipeIngredientDto;
import com.yourname.mealplanner.recipe.dto.RecipeRequest;
import com.yourname.mealplanner.recipe.dto.RecipeResponse;
import com.yourname.mealplanner.user.User;
import com.yourname.mealplanner.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecipeService recipeService;

    private User user;
    private Recipe recipe;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .fullName("Test User")
                .password("password")
                .build();

        recipe = Recipe.builder()
                .id(100L)
                .user(user)
                .title("Spaghetti Bolognese")
                .servings(4)
                .prepTimeMinutes(30)
                .instructions("Cook pasta. Add sauce.")
                .build();
    }

    @Test
    void shouldCreateRecipeSuccessfully() {
        RecipeRequest request = RecipeRequest.builder()
                .title("Spaghetti Bolognese")
                .servings(4)
                .prepTimeMinutes(30)
                .instructions("Cook pasta. Add sauce.")
                .tags(List.of("dinner", "italian"))
                .ingredients(List.of(
                        RecipeIngredientDto.builder().name("Pasta").quantity(new BigDecimal("500")).unit("g").build(),
                        RecipeIngredientDto.builder().name("Beef").quantity(new BigDecimal("400")).unit("g").build()
                ))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> {
            Recipe r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        RecipeResponse response = recipeService.createRecipe(request, 1L);

        assertNotNull(response);
        assertEquals("Spaghetti Bolognese", response.getTitle());
        assertEquals(4, response.getServings());
        assertEquals(2, response.getIngredients().size());
        assertEquals(2, response.getTags().size());
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void shouldThrowNotFoundWhenAccessingAnotherUsersRecipe() {
        when(recipeRepository.findByIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            recipeService.getRecipeById(100L, 2L);
        });
    }

    @Test
    void shouldUpdateRecipeSuccessfully() {
        RecipeRequest updateRequest = RecipeRequest.builder()
                .title("Updated Pasta")
                .servings(2)
                .prepTimeMinutes(20)
                .instructions("New instructions")
                .tags(Collections.singletonList("quick"))
                .ingredients(Collections.singletonList(
                        RecipeIngredientDto.builder().name("Pasta").quantity(new BigDecimal("250")).unit("g").build()
                ))
                .build();

        when(recipeRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);

        RecipeResponse updated = recipeService.updateRecipe(100L, updateRequest, 1L);

        assertNotNull(updated);
        assertEquals("Updated Pasta", updated.getTitle());
        assertEquals(2, updated.getServings());
        verify(recipeRepository).save(recipe);
    }

    @Test
    void shouldDeleteRecipeSuccessfully() {
        when(recipeRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(recipe));

        recipeService.deleteRecipe(100L, 1L);

        verify(recipeRepository).delete(recipe);
    }
}
