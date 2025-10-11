package com.clothsphere.repository.PM;

import com.clothsphere.model.PM.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductId(String productId);

    Optional<Product> findByCode(String code);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategory(String category);

    Optional<Product> findByName(String name);

    @Query("SELECT MAX(p.productId) FROM Product p WHERE p.productId LIKE 'P%'")
    String findMaxProductId();

    boolean existsByCode(String code);

    boolean existsByProductId(String productId);

    List<Product> findByGarmentTypeAndSizeAndFabricId(String garmentType, String size, String fabricId);

    boolean existsByGarmentTypeAndProductId(String garmentType, String productId);

    List<Product> findByGarmentType(String garmentType);

    List<Product> findBySize(String size);

    List<Product> findByFabricId(String fabricId);
}