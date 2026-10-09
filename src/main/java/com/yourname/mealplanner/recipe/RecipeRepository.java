package com.yourname.mealplanner.recipe;

import com.yourname.mealplanner.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Optional<Recipe> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    Page<Recipe> findByUser(User user, Pageable pageable);

    List<Recipe> findByUser(User user);

    Page<Recipe> findByUserAndTitleContainingIgnoreCase(User user, String title, Pageable pageable);

    Page<Recipe> findDistinctByUserAndTagsTagIgnoreCase(User user, String tag, Pageable pageable);

    Page<Recipe> findDistinctByUserAndTitleContainingIgnoreCaseAndTagsTagIgnoreCase(
            User user, String title, String tag, Pageable pageable);

    long countByUserId(Long userId);

    @Query("SELECT DISTINCT t.tag FROM RecipeTag t WHERE t.recipe.user.id = :userId ORDER BY t.tag")
    List<String> findAllTagsByUserId(@Param("userId") Long userId);
}
