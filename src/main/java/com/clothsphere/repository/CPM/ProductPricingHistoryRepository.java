package com.clothsphere.repository.CPM;

import com.clothsphere.model.CPM.ProductPricingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductPricingHistoryRepository extends JpaRepository<ProductPricingHistory, Long> {

    // Find all price changes for a specific product
    List<ProductPricingHistory> findByProductId(String productId);

    // Find price changes for a product ordered by date (newest first)
    List<ProductPricingHistory> findByProductIdOrderByChangedAtDesc(String productId);

    // Find all price changes made by a specific officer
    List<ProductPricingHistory> findByChangedBy(String changedBy);

    // Find recent price changes (limited)
    List<ProductPricingHistory> findTop10ByOrderByChangedAtDesc();
}
