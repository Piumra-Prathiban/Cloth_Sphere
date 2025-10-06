package com.clothsphere.repository;

import com.clothsphere.model.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser, String> {

    Optional<SystemUser> findByEmail(String email);

    List<SystemUser> findByRole(String role);

    List<SystemUser> findByRoleNot(String role);

    @Query("SELECT u FROM SystemUser u WHERE u.userName = :userName")
    SystemUser findByUserName(@Param("userName") String userName);

    @Query("SELECT u FROM SystemUser u ORDER BY u.role, u.userName")
    List<SystemUser> findAllOrderedByRole();

    @Query("SELECT u FROM SystemUser u WHERE u.role = :role ORDER BY u.userName")
    List<SystemUser> findByRoleOrderedByUserName(@Param("role") String role);

    @Query("SELECT COUNT(u) FROM SystemUser u")
    Long countAllSystemUsers();

    @Query("SELECT COUNT(u) FROM SystemUser u WHERE u.role = :role")
    Long countByRole(@Param("role") String role);

    @Query("SELECT DISTINCT u.role FROM SystemUser u ORDER BY u.role")
    List<String> findAllDistinctRoles();

    // Custom update password query
    @Modifying
    @Transactional
    @Query("UPDATE SystemUser u SET u.password = :password WHERE u.userName = :userName")
    int updatePassword(@Param("userName") String userName, @Param("password") String password);

    // Custom update log count query
    @Modifying
    @Transactional
    @Query("UPDATE SystemUser u SET u.logCount = :logCount WHERE u.userName = :userName")
    int updateLogCount(@Param("userName") String userName, @Param("logCount") Integer logCount);

    // Combined update for password and log count
    @Modifying
    @Transactional
    @Query("UPDATE SystemUser u SET u.password = :password, u.logCount = :logCount WHERE u.userName = :userName")
    int updatePasswordAndLogCount(@Param("userName") String userName,
                                  @Param("password") String password,
                                  @Param("logCount") Integer logCount);

    // Manual INSERT query
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO system_user_login_details (user_name, password, email, phone_number, role, log_count, created_at) " +
            "VALUES (:userName, :password, :email, :phoneNumber, :role, :logCount, :createdAt)",
            nativeQuery = true)
    int insertSystemUser(@Param("userName") String userName,
                         @Param("password") String password,
                         @Param("email") String email,
                         @Param("phoneNumber") String phoneNumber,
                         @Param("role") String role,
                         @Param("logCount") Integer logCount,
                         @Param("createdAt") java.time.LocalDateTime createdAt);

    // NEW: Manual UPDATE query for email and phone number
    @Modifying
    @Transactional
    @Query(value = "UPDATE system_user_login_details SET email = :email, phone_number = :phoneNumber WHERE user_name = :userName",
            nativeQuery = true)
    int updateSystemUserDetails(@Param("userName") String userName,
                                @Param("email") String email,
                                @Param("phoneNumber") String phoneNumber);

    // NEW: Manual UPDATE query for all fields
    @Modifying
    @Transactional
    @Query(value = "UPDATE system_user_login_details SET password = :password, email = :email, phone_number = :phoneNumber, role = :role, log_count = :logCount WHERE user_name = :userName",
            nativeQuery = true)
    int updateSystemUser(@Param("userName") String userName,
                         @Param("password") String password,
                         @Param("email") String email,
                         @Param("phoneNumber") String phoneNumber,
                         @Param("role") String role,
                         @Param("logCount") Integer logCount);

    @Query("SELECT su FROM SystemUser su WHERE su.role IN :roles")
    List<SystemUser> findByRoles(@Param("roles") List<String> roles);

    // Find users by name pattern
    @Query("SELECT su FROM SystemUser su WHERE LOWER(su.userName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<SystemUser> findByUserNameContainingIgnoreCase(@Param("name") String name);

    // Check if user exists and is active
    @Query("SELECT CASE WHEN COUNT(su) > 0 THEN true ELSE false END FROM SystemUser su WHERE su.email = :email")
    boolean existsByEmail(@Param("email") String email);

    // Get all employee emails
    @Query("SELECT su.email FROM SystemUser su WHERE su.role = 'Employee'")
    List<String> findAllEmployeeEmails();

    // Get all manager emails (non-employees)
    @Query("SELECT su.email FROM SystemUser su WHERE su.role != 'Employee'")
    List<String> findAllManagerEmails();
}