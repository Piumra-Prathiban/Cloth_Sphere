package com.clothsphere.repository.SOM;

import com.clothsphere.model.SOM.Order;
import com.clothsphere.model.SOM.OrderId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, OrderId> {

    // Manual INSERT query for creating new order
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO orders (order_type, order_id, customer_name, customer_email, " +
            "customer_phone, customer_address, product_type, quantity, unit_price, " +
            "discount_percentage, total_amount, order_notes, status, place_date, created_by) " +
            "VALUES (:orderType, :orderId, :customerName, :customerEmail, :customerPhone, " +
            ":customerAddress, :productType, :quantity, :unitPrice, :discountPercentage, " +
            ":totalAmount, :orderNotes, :status, :placeDate, :createdBy)", nativeQuery = true)
    int insertOrder(@Param("orderType") String orderType,
                    @Param("orderId") String orderId,
                    @Param("customerName") String customerName,
                    @Param("customerEmail") String customerEmail,
                    @Param("customerPhone") String customerPhone,
                    @Param("customerAddress") String customerAddress,
                    @Param("productType") String productType,
                    @Param("quantity") Integer quantity,
                    @Param("unitPrice") Double unitPrice,
                    @Param("discountPercentage") Double discountPercentage,
                    @Param("totalAmount") Double totalAmount,
                    @Param("orderNotes") String orderNotes,
                    @Param("status") String status,
                    @Param("placeDate") LocalDateTime placeDate,
                    @Param("createdBy") String createdBy);

    // Manual UPDATE query for updating order status
    @Modifying
    @Transactional
    @Query(value = "UPDATE orders SET status = :status WHERE order_type = :orderType AND order_id = :orderId",
            nativeQuery = true)
    int updateOrderStatus(@Param("orderType") String orderType,
                          @Param("orderId") String orderId,
                          @Param("status") String status);

    // Manual UPDATE query for updating order discount
    @Modifying
    @Transactional
    @Query(value = "UPDATE orders SET discount_percentage = :discountPercentage, " +
            "total_amount = :totalAmount WHERE order_type = :orderType AND order_id = :orderId",
            nativeQuery = true)
    int updateOrderDiscount(@Param("orderType") String orderType,
                            @Param("orderId") String orderId,
                            @Param("discountPercentage") Double discountPercentage,
                            @Param("totalAmount") Double totalAmount);

    // Manual DELETE query
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM orders WHERE order_type = :orderType AND order_id = :orderId",
            nativeQuery = true)
    int deleteOrder(@Param("orderType") String orderType,
                    @Param("orderId") String orderId);

    // Manual SELECT queries
    @Query(value = "SELECT * FROM orders WHERE order_type = :orderType AND order_id = :orderId",
            nativeQuery = true)
    Optional<Order> findByOrderTypeAndOrderId(@Param("orderType") String orderType,
                                              @Param("orderId") String orderId);

    @Query(value = "SELECT * FROM orders WHERE order_type = :orderType ORDER BY place_date DESC",
            nativeQuery = true)
    List<Order> findByOrderType(@Param("orderType") String orderType);

    @Query(value = "SELECT * FROM orders WHERE status = :status ORDER BY place_date DESC",
            nativeQuery = true)
    List<Order> findByStatus(@Param("status") String status);

    @Query(value = "SELECT * FROM orders WHERE customer_name LIKE CONCAT('%', :customerName, '%') " +
            "ORDER BY place_date DESC", nativeQuery = true)
    List<Order> findByCustomerNameContaining(@Param("customerName") String customerName);

    @Query(value = "SELECT MAX(order_id) FROM orders WHERE order_type = :orderType",
            nativeQuery = true)
    String findMaxOrderIdByOrderType(@Param("orderType") String orderType);

    @Query(value = "SELECT * FROM orders ORDER BY place_date DESC", nativeQuery = true)
    List<Order> findAllOrderByPlaceDateDesc();

    @Query(value = "SELECT * FROM orders WHERE order_type = :orderType AND status = :status " +
            "ORDER BY place_date DESC", nativeQuery = true)
    List<Order> findByOrderTypeAndStatus(@Param("orderType") String orderType,
                                         @Param("status") String status);

    // Manual query to check if order exists
    @Query(value = "SELECT COUNT(*) FROM orders WHERE order_type = :orderType AND order_id = :orderId",
            nativeQuery = true)
    int checkOrderExists(@Param("orderType") String orderType,
                         @Param("orderId") String orderId);

    // Manual query to get orders within date range
    @Query(value = "SELECT * FROM orders WHERE place_date BETWEEN :startDate AND :endDate " +
            "ORDER BY place_date DESC", nativeQuery = true)
    List<Order> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);
}