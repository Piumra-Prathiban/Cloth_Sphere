package com.clothsphere.repository.SalesOrder;

import com.clothsphere.model.SalesOrder.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find products by name (case-insensitive search)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Find products by category
    List<Product> findByCategory(String category);

    // Find products within price range
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    // Find products with stock available
    List<Product> findByStockQuantityGreaterThan(Integer quantity);
}
