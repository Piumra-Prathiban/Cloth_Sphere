package com.clothsphere.repository.buyerPortal;

import com.clothsphere.model.buyerPortal.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}


