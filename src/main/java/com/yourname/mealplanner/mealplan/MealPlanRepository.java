package com.yourname.mealplanner.mealplan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlanEntry, Long> {

    @Query("SELECT m FROM MealPlanEntry m " +
           "JOIN FETCH m.recipe r " +
           "LEFT JOIN FETCH r.ingredients " +
           "WHERE m.user.id = :userId AND m.planDate BETWEEN :startDate AND :endDate " +
           "ORDER BY m.planDate ASC, m.mealType ASC")
    List<MealPlanEntry> findByUserIdAndDateRangeWithRecipeAndIngredients(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT m FROM MealPlanEntry m " +
           "JOIN FETCH m.recipe r " +
           "WHERE m.user.id = :userId AND m.planDate BETWEEN :startDate AND :endDate " +
           "ORDER BY m.planDate ASC, m.mealType ASC")
    List<MealPlanEntry> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    Optional<MealPlanEntry> findByUserIdAndPlanDateAndMealType(Long userId, LocalDate planDate, MealType mealType);

    Optional<MealPlanEntry> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndPlanDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
