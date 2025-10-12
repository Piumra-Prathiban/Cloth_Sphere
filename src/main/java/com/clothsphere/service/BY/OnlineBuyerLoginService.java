package com.clothsphere.service.BY;

import com.clothsphere.model.BY.OnlineBuyerLogin;
import com.clothsphere.repository.BY.OnlineBuyerLoginRepository;
import com.clothsphere.util.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OnlineBuyerLoginService {

    @Autowired
    private OnlineBuyerLoginRepository onlineBuyerLoginRepository;

    /**
     * Validate buyer by username and password using BCrypt.
     */
    public OnlineBuyerLogin validateBuyer(String username, String password) {
        System.out.println("=== BUYER VALIDATION ===");
        System.out.println("Looking for buyer: " + username);

        Optional<OnlineBuyerLogin> buyerOpt = onlineBuyerLoginRepository.findByUsername(username);

        if (buyerOpt.isPresent()) {
            OnlineBuyerLogin buyer = buyerOpt.get();
            System.out.println("Buyer found in database: " + buyer.getUsername());

            // Check if account is active
            if (!buyer.getIsActive()) {
                System.out.println("Buyer account is deactivated");
                return null;
            }

            // Use BCrypt to compare passwords
            if (PasswordEncoder.matches(password, buyer.getPassword())) {
                System.out.println("Password matches - validation successful");
                return buyer;
            } else {
                System.out.println("Password mismatch - validation failed");
            }
        } else {
            System.out.println("Buyer not found in database");
        }

        return null;
    }

    /**
     * Register new online buyer
     */
    @Transactional
    public boolean registerBuyer(OnlineBuyerLogin buyer) {
        try {
            System.out.println("=== REGISTERING NEW BUYER ===");

            // Check if username exists
            if (onlineBuyerLoginRepository.checkUsernameExists(buyer.getUsername()) > 0) {
                System.out.println("Username already exists: " + buyer.getUsername());
                return false;
            }

            // Check if email exists
            if (onlineBuyerLoginRepository.checkEmailExists(buyer.getEmail()) > 0) {
                System.out.println("Email already exists: " + buyer.getEmail());
                return false;
            }

            // Generate buyer ID (OBY01, OBY02, etc.)
            String buyerId = generateOnlineBuyerId();
            buyer.setBuyerId(buyerId);

            // Encrypt password
            String encryptedPassword = PasswordEncoder.encryptPassword(buyer.getPassword());
            buyer.setPassword(encryptedPassword);

            // Set defaults
            buyer.setRole("buyer");
            buyer.setLogCount(0);
            buyer.setCreatedAt(LocalDateTime.now());
            buyer.setIsActive(true);

            // Insert using manual query
            int result = onlineBuyerLoginRepository.insertOnlineBuyer(
                    buyer.getBuyerId(),
                    buyer.getUsername(),
                    buyer.getPassword(),
                    buyer.getEmail(),
                    buyer.getCustomerName(),
                    buyer.getPhone(),
                    buyer.getAddress(),
                    buyer.getCompany(),
                    buyer.getRole(),
                    buyer.getLogCount(),
                    buyer.getCreatedAt().toString(),
                    buyer.getIsActive()
            );

            if (result > 0) {
                System.out.println("Buyer registered successfully with ID: " + buyerId);
                return true;
            } else {
                System.out.println("Failed to register buyer");
                return false;
            }

        } catch (Exception e) {
            System.out.println("Error registering buyer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generate next online buyer ID (OBY01, OBY02, etc.)
     */
    private String generateOnlineBuyerId() {
        String maxId = onlineBuyerLoginRepository.findMaxOnlineBuyerId();

        if (maxId == null || maxId.isEmpty()) {
            return "OBY01";
        }

        try {
            int currentNum = Integer.parseInt(maxId.substring(3));
            int nextNum = currentNum + 1;
            return String.format("OBY%02d", nextNum);
        } catch (Exception e) {
            System.out.println("Error parsing max buyer ID, defaulting to OBY01");
            return "OBY01";
        }
    }

    /**
     * Find buyer by username
     */
    public OnlineBuyerLogin findByUsername(String username) {
        return onlineBuyerLoginRepository.findByUsername(username).orElse(null);
    }

    /**
     * Find buyer by email
     */
    public OnlineBuyerLogin findByEmail(String email) {
        return onlineBuyerLoginRepository.findByEmail(email).orElse(null);
    }

    /**
     * Update buyer login count
     */
    @Transactional
    public boolean updateLogCount(String username, Integer logCount) {
        try {
            int updatedRows = onlineBuyerLoginRepository.updateLogCount(username, logCount);

            if (updatedRows > 0) {
                System.out.println("Log count updated to " + logCount + " for buyer: " + username);
                return true;
            } else {
                System.out.println("No buyer found with username: " + username);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error updating log count: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update buyer password
     */
    @Transactional
    public boolean updatePassword(String username, String newPassword) {
        try {
            String encryptedPassword = PasswordEncoder.encryptPassword(newPassword);
            int updatedRows = onlineBuyerLoginRepository.updatePassword(username, encryptedPassword);

            if (updatedRows > 0) {
                System.out.println("Password updated successfully for buyer: " + username);
                return true;
            } else {
                System.out.println("No buyer found with username: " + username);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error updating password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update buyer profile
     */
    @Transactional
    public boolean updateBuyerProfile(String buyerId, String customerName, String phone, String address, String company) {
        try {
            int updatedRows = onlineBuyerLoginRepository.updateBuyerProfile(
                    buyerId, customerName, phone, address, company);

            if (updatedRows > 0) {
                System.out.println("Profile updated successfully for buyer: " + buyerId);
                return true;
            } else {
                System.out.println("No buyer found with ID: " + buyerId);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error updating profile: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        return onlineBuyerLoginRepository.checkUsernameExists(username) > 0;
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return onlineBuyerLoginRepository.checkEmailExists(email) > 0;
    }

    /**
     * Deactivate buyer account
     */
    @Transactional
    public boolean deactivateBuyer(String buyerId) {
        try {
            int updatedRows = onlineBuyerLoginRepository.deactivateBuyer(buyerId);

            if (updatedRows > 0) {
                System.out.println("Buyer account deactivated: " + buyerId);
                return true;
            } else {
                System.out.println("No buyer found with ID: " + buyerId);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error deactivating buyer: " + e.getMessage());
            return false;
        }
    }
}
