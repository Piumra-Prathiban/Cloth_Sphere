package com.clothsphere.repository.Inventory;

import com.clothsphere.model.SystemUser;
import com.clothsphere.model.SystemUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<SystemUser, SystemUserId> {
    // Find by composite key
    Optional<SystemUser> findByUserNameAndRole(String userName, String role);
}