package com.clothsphere.repository.SOM;

import com.clothsphere.model.SOM.Buyer;
import com.clothsphere.model.SOM.BuyerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, BuyerId> {

    // Manual INSERT query for creating new buyer
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO buyers (buyer_id, email, customer_name, phone, address, company) " +
            "VALUES (:buyerId, :email, :customerName, :phone, :address, :company)",
            nativeQuery = true)
    int insertBuyer(@Param("buyerId") String buyerId,
                    @Param("email") String email,
                    @Param("customerName") String customerName,
                    @Param("phone") String phone,
                    @Param("address") String address,
                    @Param("company") String company);

    // Manual UPDATE query for updating buyer information
    @Modifying
    @Transactional
    @Query(value = "UPDATE buyers SET customer_name = :customerName, phone = :phone, " +
            "address = :address, company = :company " +
            "WHERE buyer_id = :buyerId AND email = :email",
            nativeQuery = true)
    int updateBuyer(@Param("buyerId") String buyerId,
                    @Param("email") String email,
                    @Param("customerName") String customerName,
                    @Param("phone") String phone,
                    @Param("address") String address,
                    @Param("company") String company);

    // Manual DELETE query
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM buyers WHERE buyer_id = :buyerId AND email = :email",
            nativeQuery = true)
    int deleteBuyer(@Param("buyerId") String buyerId,
                    @Param("email") String email);

    // Manual SELECT queries
    @Query(value = "SELECT * FROM buyers WHERE buyer_id = :buyerId AND email = :email",
            nativeQuery = true)
    Optional<Buyer> findByBuyerIdAndEmail(@Param("buyerId") String buyerId,
                                          @Param("email") String email);

    @Query(value = "SELECT * FROM buyers WHERE customer_name LIKE CONCAT('%', :customerName, '%')",
            nativeQuery = true)
    List<Buyer> findByCustomerNameContaining(@Param("customerName") String customerName);

    @Query(value = "SELECT * FROM buyers WHERE email = :email", nativeQuery = true)
    Optional<Buyer> findByEmail(@Param("email") String email);

    @Query(value = "SELECT MAX(buyer_id) FROM buyers WHERE buyer_id LIKE 'LBY%'",
            nativeQuery = true)
    String findMaxBuyerId();

    @Query(value = "SELECT * FROM buyers ORDER BY buyer_id", nativeQuery = true)
    List<Buyer> findAllOrderByBuyerId();

    // Manual query to check if buyer exists
    @Query(value = "SELECT COUNT(*) FROM buyers WHERE buyer_id = :buyerId AND email = :email",
            nativeQuery = true)
    int checkBuyerExists(@Param("buyerId") String buyerId,
                         @Param("email") String email);

    // Manual query to check if email already exists
    @Query(value = "SELECT COUNT(*) FROM buyers WHERE email = :email", nativeQuery = true)
    int checkEmailExists(@Param("email") String email);

    // Manual query to update buyer phone
    @Modifying
    @Transactional
    @Query(value = "UPDATE buyers SET phone = :phone WHERE buyer_id = :buyerId AND email = :email",
            nativeQuery = true)
    int updateBuyerPhone(@Param("buyerId") String buyerId,
                         @Param("email") String email,
                         @Param("phone") String phone);

    // Manual query to update buyer address
    @Modifying
    @Transactional
    @Query(value = "UPDATE buyers SET address = :address WHERE buyer_id = :buyerId AND email = :email",
            nativeQuery = true)
    int updateBuyerAddress(@Param("buyerId") String buyerId,
                           @Param("email") String email,
                           @Param("address") String address);

    // Manual query to get buyers by company
    @Query(value = "SELECT * FROM buyers WHERE company = :company ORDER BY buyer_id",
            nativeQuery = true)
    List<Buyer> findByCompany(@Param("company") String company);
}