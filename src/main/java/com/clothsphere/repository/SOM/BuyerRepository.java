package com.clothsphere.repository.SOM;

import com.clothsphere.model.SOM.Buyer;
import com.clothsphere.model.SOM.BuyerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, BuyerId> {

    // Custom query to find buyer by buyerId and email (composite key)
    @Query("SELECT b FROM Buyer b WHERE b.buyerId = :buyerId AND b.email = :email")
    Optional<Buyer> findByBuyerIdAndEmail(@Param("buyerId") String buyerId,
                                          @Param("email") String email);

    // Find all buyers by customer name
    @Query("SELECT b FROM Buyer b WHERE b.customerName LIKE %:customerName%")
    List<Buyer> findByCustomerNameContaining(@Param("customerName") String customerName);

    // Find buyer by email
    @Query("SELECT b FROM Buyer b WHERE b.email = :email")
    Optional<Buyer> findByEmail(@Param("email") String email);

    // Get the maximum buyer ID to generate next ID
    @Query("SELECT MAX(b.buyerId) FROM Buyer b WHERE b.buyerId LIKE 'LBY%'")
    String findMaxBuyerId();

    // Find all buyers ordered by buyerId
    @Query("SELECT b FROM Buyer b ORDER BY b.buyerId")
    List<Buyer> findAllOrderByBuyerId();
}