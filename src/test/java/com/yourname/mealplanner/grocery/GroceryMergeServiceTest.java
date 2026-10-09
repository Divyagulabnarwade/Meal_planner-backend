package com.yourname.mealplanner.grocery;

import com.yourname.mealplanner.mealplan.MealPlanEntry;
import com.yourname.mealplanner.mealplan.MealType;
import com.yourname.mealplanner.pantry.PantryItem;
import com.yourname.mealplanner.recipe.Recipe;
import com.yourname.mealplanner.recipe.RecipeIngredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroceryMergeServiceTest {

    private GroceryMergeService mergeService;

    @BeforeEach
    void setUp() {
        mergeService = new GroceryMergeService();
    }

    private MealPlanEntry createMealEntry(Recipe recipe, int entryServings) {
        return MealPlanEntry.builder()
                .recipe(recipe)
                .servings(entryServings)
                .planDate(LocalDate.now())
                .mealType(MealType.DINNER)
                .build();
    }

    private Recipe createRecipe(String title, int servings, List<RecipeIngredient> ingredients) {
        Recipe recipe = Recipe.builder()
                .title(title)
                .servings(servings)
                .build();
        for (RecipeIngredient ing : ingredients) {
            recipe.addIngredient(ing);
        }
        return recipe;
    }

    @Test
    void shouldMergeExactDuplicates() {
        Recipe r1 = createRecipe("Salad", 2, List.of(
                RecipeIngredient.builder().name("Cucumber").quantity(new BigDecimal("2")).unit("pcs").build()
        ));
        Recipe r2 = createRecipe("Sandwich", 2, List.of(
                RecipeIngredient.builder().name("Cucumber").quantity(new BigDecimal("3")).unit("pcs").build()
        ));

        List<MealPlanEntry> entries = List.of(createMealEntry(r1, 2), createMealEntry(r2, 2));

        List<GroceryItem> items = mergeService.generateMergedList(entries, Collections.emptyList(), true);

        assertEquals(1, items.size());
        assertEquals("cucumber", items.get(0).getName());
        assertEquals(new BigDecimal("5"), items.get(0).getQuantity());
        assertEquals("pcs", items.get(0).getUnit());
    }

    @Test
    void shouldNormalizePluralNames() {
        Recipe r1 = createRecipe("Soup", 1, List.of(
                RecipeIngredient.builder().name("Tomatoes").quantity(new BigDecimal("2")).unit("pcs").build()
        ));
        Recipe r2 = createRecipe("Pasta", 1, List.of(
                RecipeIngredient.builder().name("Tomato").quantity(new BigDecimal("1")).unit("pcs").build()
        ));

        List<MealPlanEntry> entries = List.of(createMealEntry(r1, 1), createMealEntry(r2, 1));

        List<GroceryItem> items = mergeService.generateMergedList(entries, Collections.emptyList(), true);

        assertEquals(1, items.size());
        assertEquals("tomato", items.get(0).getName());
        assertEquals(new BigDecimal("3"), items.get(0).getQuantity());
    }

    @Test
    void shouldConvertAndSumCompatibleUnitsMass() {
        // 500 g + 1.5 kg = 2000 g -> should output 2 kg
        Recipe r1 = createRecipe("Bake 1", 1, List.of(
                RecipeIngredient.builder().name("Flour").quantity(new BigDecimal("500")).unit("g").build()
        ));
        Recipe r2 = createRecipe("Bake 2", 1, List.of(
                RecipeIngredient.builder().name("Flour").quantity(new BigDecimal("1.5")).unit("kg").build()
        ));

        List<MealPlanEntry> entries = List.of(createMealEntry(r1, 1), createMealEntry(r2, 1));

        List<GroceryItem> items = mergeService.generateMergedList(entries, Collections.emptyList(), true);

        assertEquals(1, items.size());
        assertEquals("flour", items.get(0).getName());
        assertEquals(new BigDecimal("2"), items.get(0).getQuantity());
        assertEquals("kg", items.get(0).getUnit());
    }

    @Test
    void shouldConvertAndSumCookingVolumeUnits() {
        // 1 cup (= 16 tbsp) + 2 tbsp = 18 tbsp (= 1.125 cup or 18/48 = 0.375... wait 1 cup = 48 tsp, 2 tbsp = 6 tsp -> 54 tsp -> 54/48 = 1.13 cups)
        Recipe r1 = createRecipe("Cake", 1, List.of(
                RecipeIngredient.builder().name("Sugar").quantity(new BigDecimal("1")).unit("cup").build()
        ));
        Recipe r2 = createRecipe("Tea", 1, List.of(
                RecipeIngredient.builder().name("Sugar").quantity(new BigDecimal("2")).unit("tbsp").build()
        ));

        List<MealPlanEntry> entries = List.of(createMealEntry(r1, 1), createMealEntry(r2, 1));

        List<GroceryItem> items = mergeService.generateMergedList(entries, Collections.emptyList(), true);

        assertEquals(1, items.size());
        assertEquals("sugar", items.get(0).getName());
        assertEquals("cup", items.get(0).getUnit());
        assertEquals(new BigDecimal("1.13"), items.get(0).getQuantity());
    }

    @Test
    void shouldScaleQuantitiesByServings() {
        // Recipe serves 2, entry planned for 6 -> factor = 3x
        Recipe r = createRecipe("Rice Bowl", 2, List.of(
                RecipeIngredient.builder().name("Rice").quantity(new BigDecimal("100")).unit("g").build()
        ));

        List<MealPlanEntry> entries = List.of(createMealEntry(r, 6));

        List<GroceryItem> items = mergeService.generateMergedList(entries, Collections.emptyList(), true);

        assertEquals(1, items.size());
        assertEquals("rice", items.get(0).getName());
        assertEquals(new BigDecimal("300"), items.get(0).getQuantity());
        assertEquals("g", items.get(0).getUnit());
    }

    @Test
    void shouldKeepIncompatibleUnitsAsSeparateLines() {
        // 2 cups tomato vs 500 g tomato (volume vs mass)
        Recipe r1 = createRecipe("Sauce", 1, List.of(
                RecipeIngredient.builder().name("Tomato").quantity(new BigDecimal("2")).unit("cup").build()
        ));
        Recipe r2 = createRecipe("Salad", 1, List.of(
                RecipeIngredient.builder().name("Tomato").quantity(new BigDecimal("500")).unit("g").build()
        ));

        List<MealPlanEntry> entries = List.of(createMealEntry(r1, 1), createMealEntry(r2, 1));

        List<GroceryItem> items = mergeService.generateMergedList(entries, Collections.emptyList(), true);

        assertEquals(2, items.size());
        assertTrue(items.stream().anyMatch(i -> i.getUnit().equals("cup") && i.getQuantity().compareTo(new BigDecimal("2")) == 0));
        assertTrue(items.stream().anyMatch(i -> i.getUnit().equals("g") && i.getQuantity().compareTo(new BigDecimal("500")) == 0));
    }

    @Test
    void shouldSubtractPantryItemsCorrectly() {
        // Need 1 kg rice, pantry has 400 g -> remaining needed: 600 g
        Recipe r = createRecipe("Curry Rice", 1, List.of(
                RecipeIngredient.builder().name("Rice").quantity(new BigDecimal("1")).unit("kg").build()
        ));

        List<MealPlanEntry> entries = List.of(createMealEntry(r, 1));
        List<PantryItem> pantry = List.of(
                PantryItem.builder().name("Rice").quantity(new BigDecimal("400")).unit("g").build()
        );

        List<GroceryItem> items = mergeService.generateMergedList(entries, pantry, true);

        assertEquals(1, items.size());
        assertEquals("rice", items.get(0).getName());
        assertEquals(new BigDecimal("600"), items.get(0).getQuantity());
        assertEquals("g", items.get(0).getUnit());
    }

    @Test
    void shouldOmitItemWhenPantryCoversEntireQuantity() {
        // Need 500 g flour, pantry has 1 kg flour -> 0 needed (omitted)
        Recipe r = createRecipe("Pancakes", 1, List.of(
                RecipeIngredient.builder().name("Flour").quantity(new BigDecimal("500")).unit("g").build()
        ));

        List<MealPlanEntry> entries = List.of(createMealEntry(r, 1));
        List<PantryItem> pantry = List.of(
                PantryItem.builder().name("Flour").quantity(new BigDecimal("1")).unit("kg").build()
        );

        List<GroceryItem> items = mergeService.generateMergedList(entries, pantry, true);

        assertEquals(0, items.size());
    }
}
