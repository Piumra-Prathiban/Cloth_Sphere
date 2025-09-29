package com.clothsphere.repository.production;

import com.clothsphere.model.production.ProductionMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionMessageRepository extends JpaRepository<ProductionMessage, Long> {

    List<ProductionMessage> findTop6ByOrderByCreatedAtDesc();
}
