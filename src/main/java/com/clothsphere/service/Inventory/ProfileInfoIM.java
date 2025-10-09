package com.clothsphere.service.Inventory;

import com.clothsphere.model.SystemUser;
import com.clothsphere.model.SystemUserId;
import com.clothsphere.repository.Inventory.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileInfoIM implements ProfileIMService {

    @Autowired
    private ProfileRepository repository;

    @Override
    public SystemUser getUserProfile(String userName, String role) {
        Optional<SystemUser> user = repository.findByUserNameAndRole(userName, role);
        if (user.isPresent()) {
            return user.get();
        }
        throw new RuntimeException("User not found with username: " + userName + " and role: " + role);
    }

    @Override
    public SystemUser updateProfile(SystemUserId id, String email, String phoneNumber) {
        Optional<SystemUser> existing = repository.findById(id);
        if (existing.isPresent()) {
            SystemUser user = existing.get();
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            return repository.save(user);
        }
        throw new RuntimeException("User not found with ID: " + id);
    }

    @Override
    public void updatePassword(SystemUserId id, String newPassword) {
        Optional<SystemUser> existing = repository.findById(id);
        if (existing.isPresent()) {
            SystemUser user = existing.get();
            user.setPassword(newPassword);
            repository.save(user);
        } else {
            throw new RuntimeException("User not found with ID: " + id);
        }
    }
}