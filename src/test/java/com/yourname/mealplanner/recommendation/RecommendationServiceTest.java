package com.yourname.mealplanner.recommendation;

import com.yourname.mealplanner.pantry.PantryItem;
import com.yourname.mealplanner.pantry.PantryRepository;
import com.yourname.mealplanner.recipe.Recipe;
import com.yourname.mealplanner.recipe.RecipeIngredient;
import com.yourname.mealplanner.recipe.RecipeRepository;
import com.yourname.mealplanner.recipe.RecipeTag;
import com.yourname.mealplanner.recommendation.dto.RecommendationRequest;
import com.yourname.mealplanner.recommendation.dto.RecipeRecommendationDto;
import com.yourname.mealplanner.user.User;
import com.yourname.mealplanner.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private PantryRepository pantryRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private RuleBasedRecommendationEngine recommendationEngine = new RuleBasedRecommendationEngine();

    @InjectMocks
    private RecommendationService recommendationService;

    private User user;
    private Recipe recipe1;
    private Recipe recipe2;
    private PantryItem pantryItem;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@example.com").fullName("Test User").build();

        recipe1 = Recipe.builder()
                .id(10L)
                .user(user)
                .title("Tomato Pasta")
                .prepTimeMinutes(15)
                .servings(2)
                .build();
        recipe1.addTag(RecipeTag.builder().tag("vegetarian").build());
        recipe1.addIngredient(RecipeIngredient.builder().name("pasta").quantity(new BigDecimal("200")).unit("g").build());
        recipe1.addIngredient(RecipeIngredient.builder().name("tomatoes").quantity(new BigDecimal("2")).unit("pcs").build());

        recipe2 = Recipe.builder()
                .id(20L)
                .user(user)
                .title("Beef Steak")
                .prepTimeMinutes(30)
                .servings(2)
                .build();
        recipe2.addTag(RecipeTag.builder().tag("meat").build());
        recipe2.addIngredient(RecipeIngredient.builder().name("beef").quantity(new BigDecimal("500")).unit("g").build());

        pantryItem = PantryItem.builder()
                .id(100L)
                .user(user)
                .name("pasta")
                .quantity(new BigDecimal("500"))
                .unit("g")
                .build();
    }

    @Test
    void shouldRankRecipeWithPantryIngredientsHigher() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(recipeRepository.findByUser(user)).thenReturn(List.of(recipe1, recipe2));
        when(pantryRepository.findByUserIdOrderByNameAsc(1L)).thenReturn(List.of(pantryItem));

        RecommendationRequest request = RecommendationRequest.builder()
                .matchPantry(true)
                .limit(5)
                .build();

        List<RecipeRecommendationDto> results = recommendationService.getRecommendations(1L, request);

        assertNotNull(results);
        assertEquals(2, results.size());
        // recipe1 has 1 of 2 ingredients in pantry (50%), recipe2 has 0 (0%)
        assertEquals("Tomato Pasta", results.get(0).getTitle());
        assertTrue(results.get(0).getMatchScore() > results.get(1).getMatchScore());
        assertTrue(results.get(0).getMatchedIngredients().contains("pasta"));
        assertTrue(results.get(0).getMissingIngredients().contains("tomatoes"));
    }

    @Test
    void shouldFilterOrScoreByDietaryTags() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(recipeRepository.findByUser(user)).thenReturn(List.of(recipe1, recipe2));
        when(pantryRepository.findByUserIdOrderByNameAsc(1L)).thenReturn(List.of());

        RecommendationRequest request = RecommendationRequest.builder()
                .dietaryTags(List.of("vegetarian"))
                .matchPantry(false)
                .build();

        List<RecipeRecommendationDto> results = recommendationService.getRecommendations(1L, request);

        assertEquals(2, results.size());
        assertEquals("Tomato Pasta", results.get(0).getTitle());
        assertTrue(results.get(0).getMatchScore() > results.get(1).getMatchScore());
    }
}
