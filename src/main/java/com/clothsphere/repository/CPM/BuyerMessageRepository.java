package com.clothsphere.repository.CPM;

import com.clothsphere.model.CPM.BuyerMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuyerMessageRepository extends JpaRepository<BuyerMessage, String> {

    @Query("SELECT bm FROM BuyerMessage bm WHERE bm.buyerId = :buyerId")
    List<BuyerMessage> findByBuyerId(@Param("buyerId") String buyerId);

    @Query("SELECT bm FROM BuyerMessage bm WHERE bm.status = :status")
    List<BuyerMessage> findByStatus(@Param("status") String status);

    @Query("SELECT bm FROM BuyerMessage bm WHERE bm.messageType = :messageType")
    List<BuyerMessage> findByMessageType(@Param("messageType") String messageType);

    @Query("SELECT bm FROM BuyerMessage bm WHERE bm.priority = :priority")
    List<BuyerMessage> findByPriority(@Param("priority") String priority);

    @Query("SELECT bm FROM BuyerMessage bm WHERE bm.status = :status AND bm.buyerId = :buyerId")
    List<BuyerMessage> findByStatusAndBuyerId(@Param("status") String status, @Param("buyerId") String buyerId);

    @Query("SELECT COUNT(bm) FROM BuyerMessage bm WHERE bm.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT bm FROM BuyerMessage bm ORDER BY bm.createdAt DESC")
    List<BuyerMessage> findAllByOrderByCreatedAtDesc();

    @Query("SELECT bm FROM BuyerMessage bm WHERE bm.status = :status ORDER BY bm.createdAt DESC")
    List<BuyerMessage> findByStatusOrderByCreatedAtDesc(@Param("status") String status);

    @Query("SELECT bm FROM BuyerMessage bm WHERE bm.buyerId = :buyerId ORDER BY bm.createdAt DESC")
    List<BuyerMessage> findByBuyerIdOrderByCreatedAtDesc(@Param("buyerId") String buyerId);
}
