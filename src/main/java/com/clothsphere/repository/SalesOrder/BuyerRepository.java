package com.clothsphere.repository.SalesOrder;

import com.clothsphere.model.SalesOrder.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, Long> {

    // Find buyer by email
    Optional<Buyer> findByEmail(String email);

    // Find buyers by name (case-insensitive search)
    List<Buyer> findByNameContainingIgnoreCase(String name);

    // Find all active buyers
    List<Buyer> findByIsActiveTrue();

    // Find buyers by company name
    List<Buyer> findByCompanyNameContainingIgnoreCase(String companyName);
}
