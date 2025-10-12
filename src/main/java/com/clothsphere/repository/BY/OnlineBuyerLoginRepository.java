package com.clothsphere.repository.BY;

import com.clothsphere.model.BY.OnlineBuyerLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface OnlineBuyerLoginRepository extends JpaRepository<OnlineBuyerLogin, String> {

    // Find by username
    @Query(value = "SELECT * FROM online_buyer_login WHERE username = :username", nativeQuery = true)
    Optional<OnlineBuyerLogin> findByUsername(@Param("username") String username);

    // Find by email
    @Query(value = "SELECT * FROM online_buyer_login WHERE email = :email", nativeQuery = true)
    Optional<OnlineBuyerLogin> findByEmail(@Param("email") String email);

    // Check if username exists
    @Query(value = "SELECT COUNT(*) FROM online_buyer_login WHERE username = :username", nativeQuery = true)
    int checkUsernameExists(@Param("username") String username);

    // Check if email exists
    @Query(value = "SELECT COUNT(*) FROM online_buyer_login WHERE email = :email", nativeQuery = true)
    int checkEmailExists(@Param("email") String email);

    // Insert new buyer
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO online_buyer_login (buyer_id, username, password, email, customer_name, phone, address, company, role, log_count, created_at, is_active) " +
            "VALUES (:buyerId, :username, :password, :email, :customerName, :phone, :address, :company, :role, :logCount, :createdAt, :isActive)",
            nativeQuery = true)
    int insertOnlineBuyer(@Param("buyerId") String buyerId,
                         @Param("username") String username,
                         @Param("password") String password,
                         @Param("email") String email,
                         @Param("customerName") String customerName,
                         @Param("phone") String phone,
                         @Param("address") String address,
                         @Param("company") String company,
                         @Param("role") String role,
                         @Param("logCount") Integer logCount,
                         @Param("createdAt") String createdAt,
                         @Param("isActive") Boolean isActive);

    // Update log count
    @Modifying
    @Transactional
    @Query(value = "UPDATE online_buyer_login SET log_count = :logCount WHERE username = :username", nativeQuery = true)
    int updateLogCount(@Param("username") String username, @Param("logCount") Integer logCount);

    // Update password
    @Modifying
    @Transactional
    @Query(value = "UPDATE online_buyer_login SET password = :password WHERE username = :username", nativeQuery = true)
    int updatePassword(@Param("username") String username, @Param("password") String password);

    // Get max buyer ID for OBY prefix
    @Query(value = "SELECT MAX(buyer_id) FROM online_buyer_login WHERE buyer_id LIKE 'OBY%'", nativeQuery = true)
    String findMaxOnlineBuyerId();

    // Update buyer profile
    @Modifying
    @Transactional
    @Query(value = "UPDATE online_buyer_login SET customer_name = :customerName, phone = :phone, " +
            "address = :address, company = :company WHERE buyer_id = :buyerId",
            nativeQuery = true)
    int updateBuyerProfile(@Param("buyerId") String buyerId,
                          @Param("customerName") String customerName,
                          @Param("phone") String phone,
                          @Param("address") String address,
                          @Param("company") String company);

    // Deactivate buyer account
    @Modifying
    @Transactional
    @Query(value = "UPDATE online_buyer_login SET is_active = false WHERE buyer_id = :buyerId", nativeQuery = true)
    int deactivateBuyer(@Param("buyerId") String buyerId);
}
