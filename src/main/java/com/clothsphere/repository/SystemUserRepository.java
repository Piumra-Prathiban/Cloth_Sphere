package com.clothsphere.repository;

import com.clothsphere.model.SystemUser;
import com.clothsphere.model.SystemUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser, SystemUserId> {

    @Query("SELECT u FROM SystemUser u WHERE u.userName = :userName AND u.role = :role")
    SystemUser findByUserNameAndRole(@Param("userName") String userName, @Param("role") String role);

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
}


