package com.clothsphere.repository.SOM;

import com.clothsphere.model.SOM.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Find all cart items for a buyer
    List<Cart> findByBuyerIdOrderByAddedAtDesc(String buyerId);

    // Find specific cart item
    Optional<Cart> findByBuyerIdAndProductId(String buyerId, Long productId);

    // Check if product is in cart
    boolean existsByBuyerIdAndProductId(String buyerId, Long productId);

    // Count items in cart
    long countByBuyerId(String buyerId);

    // Delete specific cart item
    void deleteByBuyerIdAndProductId(String buyerId, Long productId);

    // Delete all cart items for a buyer (after checkout)
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.buyerId = :buyerId")
    void deleteAllByBuyerId(@Param("buyerId") String buyerId);

    // Get total quantity in cart
    @Query("SELECT COALESCE(SUM(c.quantity), 0) FROM Cart c WHERE c.buyerId = :buyerId")
    Integer getTotalQuantityByBuyer(@Param("buyerId") String buyerId);

    // Find cart items by product (useful for checking what buyers have this product in cart)
    List<Cart> findByProductId(Long productId);
}
