package com.clothsphere.service.BY;

import com.clothsphere.model.BY.OnlineBuyerLogin;
import com.clothsphere.repository.BY.OnlineBuyerLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class BuyerProfileService {

    @Autowired
    private OnlineBuyerLoginRepository buyerRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Get buyer profile by ID
     */
    public Optional<OnlineBuyerLogin> getBuyerProfile(String buyerId) {
        return buyerRepository.findById(buyerId);
    }

    /**
     * Update buyer profile information (excluding password)
     */
    public OnlineBuyerLogin updateProfile(String buyerId, String customerName, String email,
                                          String phone, String address) {
        Optional<OnlineBuyerLogin> buyerOpt = buyerRepository.findById(buyerId);

        if (buyerOpt.isEmpty()) {
            throw new RuntimeException("Buyer not found");
        }

        OnlineBuyerLogin buyer = buyerOpt.get();

        // Check if email is being changed and if it's already taken by another user
        if (!buyer.getEmail().equals(email)) {
            Optional<OnlineBuyerLogin> existingByEmail = buyerRepository.findByEmail(email);
            if (existingByEmail.isPresent() && !existingByEmail.get().getBuyerId().equals(buyerId)) {
                throw new RuntimeException("Email already in use by another account");
            }
        }

        // Update fields
        buyer.setCustomerName(customerName);
        buyer.setEmail(email);
        buyer.setPhone(phone);
        buyer.setAddress(address);

        return buyerRepository.save(buyer);
    }

    /**
     * Change password
     */
    public boolean changePassword(String buyerId, String currentPassword, String newPassword) {
        Optional<OnlineBuyerLogin> buyerOpt = buyerRepository.findById(buyerId);

        if (buyerOpt.isEmpty()) {
            throw new RuntimeException("Buyer not found");
        }

        OnlineBuyerLogin buyer = buyerOpt.get();

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, buyer.getPassword())) {
            return false; // Current password is incorrect
        }

        // Update to new password
        buyer.setPassword(passwordEncoder.encode(newPassword));
        buyerRepository.save(buyer);

        return true;
    }

    /**
     * Update only basic information
     */
    public OnlineBuyerLogin updateBasicInfo(String buyerId, String customerName, String phone) {
        Optional<OnlineBuyerLogin> buyerOpt = buyerRepository.findById(buyerId);

        if (buyerOpt.isEmpty()) {
            throw new RuntimeException("Buyer not found");
        }

        OnlineBuyerLogin buyer = buyerOpt.get();
        buyer.setCustomerName(customerName);
        buyer.setPhone(phone);

        return buyerRepository.save(buyer);
    }

    /**
     * Update delivery address
     */
    public OnlineBuyerLogin updateAddress(String buyerId, String address) {
        Optional<OnlineBuyerLogin> buyerOpt = buyerRepository.findById(buyerId);

        if (buyerOpt.isEmpty()) {
            throw new RuntimeException("Buyer not found");
        }

        OnlineBuyerLogin buyer = buyerOpt.get();
        buyer.setAddress(address);

        return buyerRepository.save(buyer);
    }

    /**
     * Validate password strength
     */
    public boolean isPasswordStrong(String password) {
        // At least 8 characters, contains uppercase, lowercase, and number
        if (password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        return hasUpper && hasLower && hasDigit;
    }
}
