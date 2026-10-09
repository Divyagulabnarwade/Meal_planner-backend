package com.yourname.mealplanner.pantry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PantryRepository extends JpaRepository<PantryItem, Long> {

    List<PantryItem> findByUserIdOrderByNameAsc(Long userId);

    Optional<PantryItem> findByIdAndUserId(Long id, Long userId);

    Optional<PantryItem> findByUserIdAndNameIgnoreCase(Long userId, String name);

    long countByUserId(Long userId);

    @Query("SELECT p FROM PantryItem p WHERE p.user.id = :userId AND p.expiryDate IS NOT NULL AND p.expiryDate <= :cutoffDate ORDER BY p.expiryDate ASC")
    List<PantryItem> findExpiringItems(@Param("userId") Long userId, @Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT p FROM PantryItem p WHERE p.user.id = :userId AND p.lowStockThreshold IS NOT NULL AND p.quantity <= p.lowStockThreshold ORDER BY p.quantity ASC")
    List<PantryItem> findLowStockItems(@Param("userId") Long userId);

    @Query("SELECT COUNT(p) FROM PantryItem p WHERE p.user.id = :userId AND p.expiryDate IS NOT NULL AND p.expiryDate <= :cutoffDate")
    long countExpiringItems(@Param("userId") Long userId, @Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT COUNT(p) FROM PantryItem p WHERE p.user.id = :userId AND p.lowStockThreshold IS NOT NULL AND p.quantity <= p.lowStockThreshold")
    long countLowStockItems(@Param("userId") Long userId);
}
