package com.clothsphere.repository.buyerPortal;

import com.clothsphere.model.buyerPortal.Order;
import com.clothsphere.model.buyerPortal.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyer(Buyer buyer);
}
