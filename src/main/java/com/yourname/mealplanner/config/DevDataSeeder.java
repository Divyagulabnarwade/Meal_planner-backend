package com.yourname.mealplanner.config;

import com.yourname.mealplanner.mealplan.MealPlanEntry;
import com.yourname.mealplanner.mealplan.MealPlanRepository;
import com.yourname.mealplanner.mealplan.MealType;
import com.yourname.mealplanner.pantry.PantryItem;
import com.yourname.mealplanner.pantry.PantryRepository;
import com.yourname.mealplanner.recipe.Recipe;
import com.yourname.mealplanner.recipe.RecipeIngredient;
import com.yourname.mealplanner.recipe.RecipeRepository;
import com.yourname.mealplanner.recipe.RecipeTag;
import com.yourname.mealplanner.user.User;
import com.yourname.mealplanner.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final MealPlanRepository mealPlanRepository;
    private final PantryRepository pantryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        String demoEmail = "demo@mealplanner.com";
        if (userRepository.existsByEmail(demoEmail)) {
            log.info("Demo user already exists, skipping dev data seeding.");
            return;
        }

        log.info("Seeding initial demo user, recipes, meal plans, and pantry items...");

        // 1. Create Demo User
        User demoUser = User.builder()
                .email(demoEmail)
                .fullName("Demo Chef")
                .password(passwordEncoder.encode("Password123!"))
                .build();
        demoUser = userRepository.save(demoUser);

        // 2. Create 5 Sample Recipes with overlapping ingredients and mixed units
        Recipe r1 = Recipe.builder()
                .user(demoUser)
                .title("Classic Beef Bolognese")
                .description("Rich and savory slow-simmered Italian beef sauce over al dente pasta.")
                .servings(4)
                .prepTimeMinutes(35)
                .calories(650)
                .proteinGrams(new BigDecimal("38.5"))
                .carbsGrams(new BigDecimal("72.0"))
                .fatGrams(new BigDecimal("22.0"))
                .instructions("1. Heat olive oil in a pan and sauté chopped onions and garlic until translucent.\n" +
                        "2. Add ground beef, browning evenly.\n" +
                        "3. Stir in chopped tomatoes and season with salt. Simmer for 20 minutes.\n" +
                        "4. Boil pasta according to package directions. Combine and serve hot.")
                .build();
        r1.addTag(RecipeTag.builder().tag("italian").build());
        r1.addTag(RecipeTag.builder().tag("dinner").build());
        r1.addTag(RecipeTag.builder().tag("pasta").build());
        r1.addIngredient(RecipeIngredient.builder().name("Pasta").quantity(new BigDecimal("500")).unit("g").build());
        r1.addIngredient(RecipeIngredient.builder().name("Ground Beef").quantity(new BigDecimal("400")).unit("g").build());
        r1.addIngredient(RecipeIngredient.builder().name("Onions").quantity(new BigDecimal("2")).unit("pcs").build());
        r1.addIngredient(RecipeIngredient.builder().name("Garlic").quantity(new BigDecimal("3")).unit("cloves").build());
        r1.addIngredient(RecipeIngredient.builder().name("Tomatoes").quantity(new BigDecimal("4")).unit("pcs").build());
        r1.addIngredient(RecipeIngredient.builder().name("Olive Oil").quantity(new BigDecimal("2")).unit("tbsp").build());
        r1.addIngredient(RecipeIngredient.builder().name("Salt").quantity(new BigDecimal("1")).unit("tsp").build());
        r1 = recipeRepository.save(r1);

        Recipe r2 = Recipe.builder()
                .user(demoUser)
                .title("Creamy Tomato Basil Pasta")
                .description("A velvety 20-minute weeknight vegetarian pasta bursting with fresh basil flavor.")
                .servings(2)
                .prepTimeMinutes(20)
                .calories(520)
                .proteinGrams(new BigDecimal("14.0"))
                .carbsGrams(new BigDecimal("68.0"))
                .fatGrams(new BigDecimal("21.5"))
                .instructions("1. Sauté minced garlic in olive oil.\n" +
                        "2. Add fresh crushed tomatoes and simmer for 10 minutes.\n" +
                        "3. Lower heat, stir in heavy cream and fresh basil leaves.\n" +
                        "4. Toss with cooked pasta.")
                .build();
        r2.addTag(RecipeTag.builder().tag("vegetarian").build());
        r2.addTag(RecipeTag.builder().tag("quick").build());
        r2.addTag(RecipeTag.builder().tag("dinner").build());
        r2.addIngredient(RecipeIngredient.builder().name("Pasta").quantity(new BigDecimal("250")).unit("g").build());
        r2.addIngredient(RecipeIngredient.builder().name("Tomatoes").quantity(new BigDecimal("3")).unit("pcs").build());
        r2.addIngredient(RecipeIngredient.builder().name("Olive Oil").quantity(new BigDecimal("1")).unit("tbsp").build());
        r2.addIngredient(RecipeIngredient.builder().name("Garlic").quantity(new BigDecimal("2")).unit("cloves").build());
        r2.addIngredient(RecipeIngredient.builder().name("Heavy Cream").quantity(new BigDecimal("200")).unit("ml").build());
        r2.addIngredient(RecipeIngredient.builder().name("Basil").quantity(new BigDecimal("1")).unit("bunch").build());
        r2 = recipeRepository.save(r2);

        Recipe r3 = Recipe.builder()
                .user(demoUser)
                .title("Hearty Vegetable Stew")
                .description("Nutritious and comforting one-pot stew filled with root vegetables.")
                .servings(4)
                .prepTimeMinutes(45)
                .calories(290)
                .proteinGrams(new BigDecimal("7.5"))
                .carbsGrams(new BigDecimal("54.0"))
                .fatGrams(new BigDecimal("6.0"))
                .instructions("1. Chop potatoes, carrots, and onions into chunks.\n" +
                        "2. Sauté onions in olive oil until golden.\n" +
                        "3. Add potatoes, carrots, tomatoes, and vegetable broth.\n" +
                        "4. Bring to a boil, cover, and simmer for 35 minutes until tender.")
                .build();
        r3.addTag(RecipeTag.builder().tag("vegan").build());
        r3.addTag(RecipeTag.builder().tag("healthy").build());
        r3.addIngredient(RecipeIngredient.builder().name("Potatoes").quantity(new BigDecimal("4")).unit("pcs").build());
        r3.addIngredient(RecipeIngredient.builder().name("Carrots").quantity(new BigDecimal("3")).unit("pcs").build());
        r3.addIngredient(RecipeIngredient.builder().name("Onion").quantity(new BigDecimal("1")).unit("pcs").build());
        r3.addIngredient(RecipeIngredient.builder().name("Tomato").quantity(new BigDecimal("2")).unit("pcs").build());
        r3.addIngredient(RecipeIngredient.builder().name("Vegetable Broth").quantity(new BigDecimal("1")).unit("l").build());
        r3.addIngredient(RecipeIngredient.builder().name("Olive Oil").quantity(new BigDecimal("2")).unit("tbsp").build());
        r3.addIngredient(RecipeIngredient.builder().name("Salt").quantity(new BigDecimal("1.5")).unit("tsp").build());
        r3 = recipeRepository.save(r3);

        Recipe r4 = Recipe.builder()
                .user(demoUser)
                .title("Fluffy Breakfast Pancakes")
                .description("Golden, pillowy pancakes perfect for a weekend morning treat.")
                .servings(2)
                .prepTimeMinutes(15)
                .calories(380)
                .proteinGrams(new BigDecimal("9.0"))
                .carbsGrams(new BigDecimal("58.0"))
                .fatGrams(new BigDecimal("12.0"))
                .instructions("1. Whisk together flour, sugar, and baking powder.\n" +
                        "2. Beat in milk, eggs, and melted butter until batter is smooth.\n" +
                        "3. Pour onto a lightly greased hot griddle.\n" +
                        "4. Flip when bubbles form and cook until golden brown.")
                .build();
        r4.addTag(RecipeTag.builder().tag("breakfast").build());
        r4.addTag(RecipeTag.builder().tag("sweet").build());
        r4.addIngredient(RecipeIngredient.builder().name("Flour").quantity(new BigDecimal("200")).unit("g").build());
        r4.addIngredient(RecipeIngredient.builder().name("Milk").quantity(new BigDecimal("250")).unit("ml").build());
        r4.addIngredient(RecipeIngredient.builder().name("Eggs").quantity(new BigDecimal("2")).unit("pcs").build());
        r4.addIngredient(RecipeIngredient.builder().name("Sugar").quantity(new BigDecimal("2")).unit("tbsp").build());
        r4.addIngredient(RecipeIngredient.builder().name("Butter").quantity(new BigDecimal("50")).unit("g").build());
        r4 = recipeRepository.save(r4);

        Recipe r5 = Recipe.builder()
                .user(demoUser)
                .title("Mediterranean Chicken Salad")
                .description("Crisp garden salad topped with herb-seasoned grilled chicken breast.")
                .servings(2)
                .prepTimeMinutes(20)
                .calories(420)
                .proteinGrams(new BigDecimal("44.0"))
                .carbsGrams(new BigDecimal("12.0"))
                .fatGrams(new BigDecimal("21.0"))
                .instructions("1. Season chicken breast and grill until cooked through. Slice into strips.\n" +
                        "2. Chop cucumbers and tomatoes into bite-sized pieces.\n" +
                        "3. Combine in a large bowl with crumbled feta.\n" +
                        "4. Dress with olive oil, lemon juice, and a pinch of salt.")
                .build();
        r5.addTag(RecipeTag.builder().tag("salad").build());
        r5.addTag(RecipeTag.builder().tag("lunch").build());
        r5.addTag(RecipeTag.builder().tag("high-protein").build());
        r5.addIngredient(RecipeIngredient.builder().name("Chicken Breast").quantity(new BigDecimal("300")).unit("g").build());
        r5.addIngredient(RecipeIngredient.builder().name("Cucumber").quantity(new BigDecimal("1")).unit("pcs").build());
        r5.addIngredient(RecipeIngredient.builder().name("Tomatoes").quantity(new BigDecimal("2")).unit("pcs").build());
        r5.addIngredient(RecipeIngredient.builder().name("Olive Oil").quantity(new BigDecimal("2")).unit("tbsp").build());
        r5.addIngredient(RecipeIngredient.builder().name("Lemon Juice").quantity(new BigDecimal("1")).unit("tbsp").build());
        r5.addIngredient(RecipeIngredient.builder().name("Feta Cheese").quantity(new BigDecimal("100")).unit("g").build());
        r5 = recipeRepository.save(r5);

        // 3. Seed Meal Plans for Current Week
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate wednesday = monday.plusDays(2);
        LocalDate friday = monday.plusDays(4);

        mealPlanRepository.save(MealPlanEntry.builder()
                .user(demoUser).recipe(r4).planDate(monday).mealType(MealType.BREAKFAST).servings(2).build());
        mealPlanRepository.save(MealPlanEntry.builder()
                .user(demoUser).recipe(r1).planDate(monday).mealType(MealType.DINNER).servings(4).build());
        mealPlanRepository.save(MealPlanEntry.builder()
                .user(demoUser).recipe(r5).planDate(wednesday).mealType(MealType.LUNCH).servings(2).build());
        mealPlanRepository.save(MealPlanEntry.builder()
                .user(demoUser).recipe(r2).planDate(wednesday).mealType(MealType.DINNER).servings(2).build());
        mealPlanRepository.save(MealPlanEntry.builder()
                .user(demoUser).recipe(r3).planDate(friday).mealType(MealType.DINNER).servings(4).build());

        // 4. Seed Pantry Items (demonstrating pantry deduction, expiry alerts, low stock)
        pantryRepository.saveAll(List.of(
                PantryItem.builder()
                        .user(demoUser)
                        .name("olive oil")
                        .quantity(new BigDecimal("5"))
                        .unit("tbsp")
                        .lowStockThreshold(new BigDecimal("2"))
                        .expiryDate(today.plusDays(60))
                        .build(),
                PantryItem.builder()
                        .user(demoUser)
                        .name("onion")
                        .quantity(new BigDecimal("1"))
                        .unit("pcs")
                        .lowStockThreshold(new BigDecimal("2")) // Low stock!
                        .expiryDate(today.plusDays(10))
                        .build(),
                PantryItem.builder()
                        .user(demoUser)
                        .name("salt")
                        .quantity(new BigDecimal("500"))
                        .unit("g")
                        .lowStockThreshold(new BigDecimal("50"))
                        .expiryDate(today.plusDays(365))
                        .build(),
                PantryItem.builder()
                        .user(demoUser)
                        .name("milk")
                        .quantity(new BigDecimal("150"))
                        .unit("ml")
                        .lowStockThreshold(new BigDecimal("200")) // Low stock & expiring soon!
                        .expiryDate(today.plusDays(3))
                        .build(),
                PantryItem.builder()
                        .user(demoUser)
                        .name("pasta")
                        .quantity(new BigDecimal("1000"))
                        .unit("g")
                        .lowStockThreshold(new BigDecimal("250"))
                        .expiryDate(today.plusDays(120))
                        .build()
        ));

        log.info("Demo data seeding completed successfully! Login with: demo@mealplanner.com / Password123!");
    }
}
