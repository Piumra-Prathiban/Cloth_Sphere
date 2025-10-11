package com.clothsphere.repository.SOM;

import com.clothsphere.model.SOM.OnlineOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OnlineOrderItemRepository extends JpaRepository<OnlineOrderItem, Long> {

    // Find all items in an order
    List<OnlineOrderItem> findByOrder_OrderId(Long orderId);

    // Find all orders containing a specific product
    List<OnlineOrderItem> findByProductId(Long productId);

    // Count items in an order
    long countByOrder_OrderId(Long orderId);

    // Get total quantity of items in an order
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OnlineOrderItem oi WHERE oi.order.orderId = :orderId")
    Integer getTotalQuantityByOrder(@Param("orderId") Long orderId);

    // Get most ordered products
    @Query("SELECT oi.productId, oi.productName, SUM(oi.quantity) as totalOrdered " +
           "FROM OnlineOrderItem oi " +
           "GROUP BY oi.productId, oi.productName " +
           "ORDER BY totalOrdered DESC")
    List<Object[]> getMostOrderedProducts();

    // Delete all items in an order
    void deleteByOrder_OrderId(Long orderId);
}
