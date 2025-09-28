package com.clothsphere.repository.ProductManagement;

import com.clothsphere.model.ProductManagement.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends JpaRepository<Products, Long> {
    boolean existsByCode(String code); // optional check for duplicate product codes
}