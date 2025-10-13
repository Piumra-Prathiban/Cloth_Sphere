package com.clothsphere.repository.CPM;

import com.clothsphere.model.CPM.ProductPricingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductPricingHistoryRepository extends JpaRepository<ProductPricingHistory, Long> {

    @Query("SELECT ph FROM ProductPricingHistory ph WHERE ph.productId = :productId")
    List<ProductPricingHistory> findByProductId(@Param("productId") String productId);

    @Query("SELECT ph FROM ProductPricingHistory ph WHERE ph.productId = :productId ORDER BY ph.changedAt DESC")
    List<ProductPricingHistory> findByProductIdOrderByChangedAtDesc(@Param("productId") String productId);

    @Query("SELECT ph FROM ProductPricingHistory ph WHERE ph.changedBy = :changedBy")
    List<ProductPricingHistory> findByChangedBy(@Param("changedBy") String changedBy);

    @Query("SELECT ph FROM ProductPricingHistory ph ORDER BY ph.changedAt DESC LIMIT 10")
    List<ProductPricingHistory> findTop10ByOrderByChangedAtDesc();
}
