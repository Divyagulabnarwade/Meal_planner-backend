package com.yourname.mealplanner.grocery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroceryItemRepository extends JpaRepository<GroceryItem, Long> {

    @Query("SELECT i FROM GroceryItem i WHERE i.id = :itemId AND i.groceryList.user.id = :userId")
    Optional<GroceryItem> findByIdAndUserId(@Param("itemId") Long itemId, @Param("userId") Long userId);
}
