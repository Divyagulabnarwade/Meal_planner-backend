package com.yourname.mealplanner.recipe;

import com.yourname.mealplanner.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer servings;

    @Column(name = "prep_time_minutes")
    private Integer prepTimeMinutes;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "protein_grams", precision = 6, scale = 1)
    private BigDecimal proteinGrams;

    @Column(name = "carbs_grams", precision = 6, scale = 1)
    private BigDecimal carbsGrams;

    @Column(name = "fat_grams", precision = 6, scale = 1)
    private BigDecimal fatGrams;

    @Builder.Default
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RecipeTag> tags = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
        ingredient.setRecipe(this);
    }

    public void removeIngredient(RecipeIngredient ingredient) {
        ingredients.remove(ingredient);
        ingredient.setRecipe(null);
    }

    public void addTag(RecipeTag tag) {
        tags.add(tag);
        tag.setRecipe(this);
    }

    public void removeTag(RecipeTag tag) {
        tags.remove(tag);
        tag.setRecipe(null);
    }

    public void clearIngredients() {
        ingredients.forEach(ing -> ing.setRecipe(null));
        ingredients.clear();
    }

    public void clearTags() {
        tags.forEach(t -> t.setRecipe(null));
        tags.clear();
    }

    public List<String> getDietaryTagList() {
        if (tags == null) {
            return new ArrayList<>();
        }
        return tags.stream().map(RecipeTag::getTag).collect(java.util.stream.Collectors.toList());
    }
}
