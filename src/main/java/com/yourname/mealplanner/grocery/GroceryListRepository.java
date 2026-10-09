package com.yourname.mealplanner.grocery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroceryListRepository extends JpaRepository<GroceryList, Long> {

    @Query("SELECT g FROM GroceryList g LEFT JOIN FETCH g.items WHERE g.id = :id AND g.user.id = :userId")
    Optional<GroceryList> findByIdAndUserIdWithItems(@Param("id") Long id, @Param("userId") Long userId);

    Optional<GroceryList> findByIdAndUserId(Long id, Long userId);

    List<GroceryList> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COUNT(i) FROM GroceryItem i WHERE i.groceryList.user.id = :userId AND i.checked = false")
    long countUncheckedItemsByUserId(@Param("userId") Long userId);
}
