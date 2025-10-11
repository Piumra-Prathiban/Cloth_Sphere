package com.clothsphere.repository.SOM;

import com.clothsphere.model.SOM.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // Find all wishlist items for a buyer
    List<Wishlist> findByBuyerIdOrderByAddedAtDesc(String buyerId);

    // Find specific wishlist item
    Optional<Wishlist> findByBuyerIdAndProductId(String buyerId, Long productId);

    // Check if product is in wishlist
    boolean existsByBuyerIdAndProductId(String buyerId, Long productId);

    // Count items in wishlist
    long countByBuyerId(String buyerId);

    // Delete specific wishlist item
    void deleteByBuyerIdAndProductId(String buyerId, Long productId);

    // Delete all wishlist items for a buyer
    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.buyerId = :buyerId")
    void deleteAllByBuyerId(@Param("buyerId") String buyerId);

    // Find wishlist items by product (useful for notifications)
    List<Wishlist> findByProductId(Long productId);

    // Count how many buyers have this product in wishlist
    long countByProductId(Long productId);
}
