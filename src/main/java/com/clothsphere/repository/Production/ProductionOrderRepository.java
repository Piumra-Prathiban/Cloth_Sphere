package com.clothsphere.repository.Production;

import com.clothsphere.model.Production.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, String> {

    // Find orders by status
    List<ProductionOrder> findByStatus(String status);

    // Find orders by priority
    List<ProductionOrder> findByPriority(String priority);

    // Find orders by customer
    List<ProductionOrder> findByCustomerId(String customerId);

    // Find orders with deadline before a certain date
    List<ProductionOrder> findByDeadlineBefore(LocalDate date);

    // Find orders within a date range
    @Query("SELECT po FROM ProductionOrder po WHERE po.orderDate BETWEEN :startDate AND :endDate")
    List<ProductionOrder> findOrdersByDateRange(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    // Find delayed orders (status is DELAYED)
    @Query("SELECT po FROM ProductionOrder po WHERE po.status = 'DELAYED'")
    List<ProductionOrder> findDelayedOrders();

    // Find orders by status and priority
    @Query("SELECT po FROM ProductionOrder po WHERE po.status = :status AND po.priority = :priority")
    List<ProductionOrder> findByStatusAndPriority(@Param("status") String status,
                                                   @Param("priority") String priority);

    // Count orders by status
    @Query("SELECT COUNT(po) FROM ProductionOrder po WHERE po.status = :status")
    Long countByStatus(@Param("status") String status);

    // Get all order IDs for ID generation
    @Query("SELECT po.orderId FROM ProductionOrder po")
    List<String> findAllOrderIds();

    // Find upcoming orders (deadline within next N days)
    @Query("SELECT po FROM ProductionOrder po WHERE po.deadline BETWEEN :today AND :futureDate AND po.status != 'COMPLETED'")
    List<ProductionOrder> findUpcomingOrders(@Param("today") LocalDate today,
                                            @Param("futureDate") LocalDate futureDate);
}