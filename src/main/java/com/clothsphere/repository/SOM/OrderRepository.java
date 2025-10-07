package com.clothsphere.repository.SOM;

import com.clothsphere.model.SOM.Order;
import com.clothsphere.model.SOM.OrderId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, OrderId> {

    // Find orders by order type and order ID
    @Query("SELECT o FROM Order o WHERE o.orderType = :orderType AND o.orderId = :orderId")
    Optional<Order> findByOrderTypeAndOrderId(@Param("orderType") String orderType,
                                              @Param("orderId") String orderId);

    // Find all orders by order type
    @Query("SELECT o FROM Order o WHERE o.orderType = :orderType ORDER BY o.placeDate DESC")
    List<Order> findByOrderType(@Param("orderType") String orderType);

    // Find orders by status
    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.placeDate DESC")
    List<Order> findByStatus(@Param("status") String status);

    // Find orders by customer name
    @Query("SELECT o FROM Order o WHERE o.customerName LIKE %:customerName% ORDER BY o.placeDate DESC")
    List<Order> findByCustomerNameContaining(@Param("customerName") String customerName);

    // FIXED: Get the maximum order ID for a specific order type
    // This now properly handles the prefix pattern matching
    @Query("SELECT MAX(o.orderId) FROM Order o WHERE o.orderType = :orderType")
    String findMaxOrderIdByOrderType(@Param("orderType") String orderType);

    // Find all orders ordered by place date
    @Query("SELECT o FROM Order o ORDER BY o.placeDate DESC")
    List<Order> findAllOrderByPlaceDateDesc();

    // Find orders by order type and status
    @Query("SELECT o FROM Order o WHERE o.orderType = :orderType AND o.status = :status ORDER BY o.placeDate DESC")
    List<Order> findByOrderTypeAndStatus(@Param("orderType") String orderType,
                                         @Param("status") String status);
}