package com.clothsphere.repository.FM;

import com.clothsphere.model.FM.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, String> {

    // ===================== MANUAL INSERT/UPDATE/DELETE =====================

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO production_order (order_id, product_name, product_type, quantity, " +
            "order_date, deadline, priority, status, customer_name, customer_id, completed_quantity, " +
            "created_at, updated_at, notes) VALUES (:orderId, :productName, :productType, :quantity, " +
            ":orderDate, :deadline, :priority, :status, :customerName, :customerId, :completedQuantity, " +
            "NOW(), NOW(), :notes)", nativeQuery = true)
    void insertProductionOrder(@Param("orderId") String orderId,
                               @Param("productName") String productName,
                               @Param("productType") String productType,
                               @Param("quantity") Integer quantity,
                               @Param("orderDate") LocalDate orderDate,
                               @Param("deadline") LocalDate deadline,
                               @Param("priority") String priority,
                               @Param("status") String status,
                               @Param("customerName") String customerName,
                               @Param("customerId") String customerId,
                               @Param("completedQuantity") Integer completedQuantity,
                               @Param("notes") String notes);

    @Modifying
    @Transactional
    @Query(value = "UPDATE production_order SET product_name = :productName, product_type = :productType, " +
            "quantity = :quantity, deadline = :deadline, priority = :priority, status = :status, " +
            "customer_name = :customerName, completed_quantity = :completedQuantity, " +
            "updated_at = NOW(), notes = :notes WHERE order_id = :orderId", nativeQuery = true)
    int updateProductionOrder(@Param("orderId") String orderId,
                              @Param("productName") String productName,
                              @Param("productType") String productType,
                              @Param("quantity") Integer quantity,
                              @Param("deadline") LocalDate deadline,
                              @Param("priority") String priority,
                              @Param("status") String status,
                              @Param("customerName") String customerName,
                              @Param("completedQuantity") Integer completedQuantity,
                              @Param("notes") String notes);

    @Modifying
    @Transactional
    @Query(value = "UPDATE production_order SET status = :status, updated_at = NOW() WHERE order_id = :orderId", nativeQuery = true)
    int updateOrderStatus(@Param("orderId") String orderId, @Param("status") String status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE production_order SET completed_quantity = :completedQuantity, updated_at = NOW() WHERE order_id = :orderId", nativeQuery = true)
    int updateCompletedQuantity(@Param("orderId") String orderId, @Param("completedQuantity") Integer completedQuantity);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM production_order WHERE order_id = :orderId", nativeQuery = true)
    int deleteProductionOrder(@Param("orderId") String orderId);

    // ===================== EXISTING QUERY METHODS =====================

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

    // ===================== STORED PROCEDURE CALLS =====================

    /**
     * Call stored procedure to generate production summary report
     */
    @Query(value = "EXEC dbo.generate_production_report @start_date = :startDate, @end_date = :endDate", nativeQuery = true)
    List<Object[]> generateProductionReport(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * Call stored procedure to get production overview
     */
    @Query(value = "EXEC dbo.get_production_overview @start_date = :startDate, @end_date = :endDate", nativeQuery = true)
    List<Object[]> getProductionOverview(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
}