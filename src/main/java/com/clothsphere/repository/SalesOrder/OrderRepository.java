package com.clothsphere.repository.SalesOrder;

import com.clothsphere.model.SalesOrder.Order;
import com.clothsphere.model.SalesOrder.OrderStatus;
import com.clothsphere.model.SalesOrder.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find order by order number
    Optional<Order> findByOrderNumber(String orderNumber);

    // Find orders by buyer
    List<Order> findByBuyer(Buyer buyer);

    // Find orders by status
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    // Find orders by buyer name (case-insensitive)
    @Query("SELECT o FROM Order o WHERE LOWER(o.buyer.name) LIKE LOWER(CONCAT('%', :buyerName, '%'))")
    List<Order> findByBuyerNameContaining(@Param("buyerName") String buyerName);

    // Find orders by date range
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find orders by delivery deadline range
    List<Order> findByDeliveryDeadlineBetween(LocalDate startDate, LocalDate endDate);

    // Find overdue orders
    @Query("SELECT o FROM Order o WHERE o.deliveryDeadline < :currentDate AND o.orderStatus NOT IN :excludedStatuses")
    List<Order> findOverdueOrders(@Param("currentDate") LocalDate currentDate, @Param("excludedStatuses") List<OrderStatus> excludedStatuses);

    // Find recent orders (top 10)
    List<Order> findTop10ByOrderByOrderDateDesc();

    // Count orders by status
    long countByOrderStatus(OrderStatus orderStatus);

    // Search orders with multiple filters
    @Query("SELECT o FROM Order o WHERE " +
            "(:buyerName IS NULL OR LOWER(o.buyer.name) LIKE LOWER(CONCAT('%', :buyerName, '%'))) AND " +
            "(:orderStatus IS NULL OR o.orderStatus = :orderStatus) AND " +
            "(:startDate IS NULL OR o.orderDate >= :startDate) AND " +
            "(:endDate IS NULL OR o.orderDate <= :endDate)")
    List<Order> searchOrders(@Param("buyerName") String buyerName,
                             @Param("orderStatus") OrderStatus orderStatus,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);
}