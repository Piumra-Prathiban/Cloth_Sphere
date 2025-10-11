package com.clothsphere.repository.SOM;

import com.clothsphere.model.SOM.OnlineOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OnlineOrderRepository extends JpaRepository<OnlineOrder, Long> {

    // Find by order number
    Optional<OnlineOrder> findByOrderNumber(String orderNumber);

    // Find all orders by buyer
    List<OnlineOrder> findByBuyerIdOrderByOrderDateDesc(String buyerId);

    // Find orders by buyer and status
    List<OnlineOrder> findByBuyerIdAndStatusOrderByOrderDateDesc(String buyerId, String status);

    // Find orders by status
    List<OnlineOrder> findByStatusOrderByOrderDateDesc(String status);

    // Find orders by buyer between dates
    List<OnlineOrder> findByBuyerIdAndOrderDateBetweenOrderByOrderDateDesc(
        String buyerId, LocalDateTime startDate, LocalDateTime endDate);

    // Find recent orders (last N days)
    @Query("SELECT o FROM OnlineOrder o WHERE o.buyerId = :buyerId " +
           "AND o.orderDate >= :startDate ORDER BY o.orderDate DESC")
    List<OnlineOrder> findRecentOrdersByBuyer(
        @Param("buyerId") String buyerId,
        @Param("startDate") LocalDateTime startDate);

    // Count orders by buyer
    long countByBuyerId(String buyerId);

    // Count orders by status
    long countByStatus(String status);

    // Count pending orders by buyer
    long countByBuyerIdAndStatus(String buyerId, String status);

    // Find orders with payment status
    List<OnlineOrder> findByPaymentStatusOrderByOrderDateDesc(String paymentStatus);

    // Check if order number exists
    boolean existsByOrderNumber(String orderNumber);

    // Get total order amount by buyer
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OnlineOrder o WHERE o.buyerId = :buyerId")
    Double getTotalOrderAmountByBuyer(@Param("buyerId") String buyerId);

    // Get orders by delivery city
    List<OnlineOrder> findByDeliveryCityOrderByOrderDateDesc(String city);
}
