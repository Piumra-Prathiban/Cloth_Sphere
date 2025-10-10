package com.clothsphere.service.SOM;

import com.clothsphere.model.SOM.Buyer;
import com.clothsphere.repository.SOM.BuyerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BuyerService {

    @Autowired
    private BuyerRepository buyerRepository;

    // Generate next buyer ID (LBY01, LBY02, etc.)
    public String generateNextBuyerId() {
        String maxId = buyerRepository.findMaxBuyerId();
        if (maxId == null) {
            return "LBY01";
        }

        try {
            int number = Integer.parseInt(maxId.substring(3));
            number++;
            return String.format("LBY%02d", number);
        } catch (NumberFormatException e) {
            return "LBY01";
        }
    }

    // Create new buyer using manual insert
    @Transactional
    public Buyer createBuyer(Buyer buyer) {
        // Check if buyer with same email already exists using manual query
        int emailExists = buyerRepository.checkEmailExists(buyer.getEmail());
        if (emailExists > 0) {
            throw new RuntimeException("Buyer with email " + buyer.getEmail() + " already exists");
        }

        // Generate buyer ID if not provided
        if (buyer.getBuyerId() == null || buyer.getBuyerId().isEmpty()) {
            buyer.setBuyerId(generateNextBuyerId());
        }

        // Use manual insert query
        int result = buyerRepository.insertBuyer(
                buyer.getBuyerId(),
                buyer.getEmail(),
                buyer.getCustomerName(),
                buyer.getPhone(),
                buyer.getAddress(),
                buyer.getCompany()
        );

        if (result > 0) {
            System.out.println("Buyer created successfully: " + buyer.getBuyerId());
            return buyer;
        } else {
            throw new RuntimeException("Failed to create buyer");
        }
    }

    // Get all buyers
    public List<Buyer> getAllBuyers() {
        return buyerRepository.findAllOrderByBuyerId();
    }

    // Get buyer by composite key
    public Optional<Buyer> getBuyerById(String buyerId, String email) {
        return buyerRepository.findByBuyerIdAndEmail(buyerId, email);
    }

    // Get buyer by email
    public Optional<Buyer> getBuyerByEmail(String email) {
        return buyerRepository.findByEmail(email);
    }

    // Update buyer using manual update
    @Transactional
    public Buyer updateBuyer(String buyerId, String email, Buyer buyerDetails) {
        // Check if buyer exists
        int exists = buyerRepository.checkBuyerExists(buyerId, email);
        if (exists == 0) {
            throw new RuntimeException("Buyer not found with ID: " + buyerId + " and email: " + email);
        }

        // Use manual update query
        int result = buyerRepository.updateBuyer(
                buyerId,
                email,
                buyerDetails.getCustomerName(),
                buyerDetails.getPhone(),
                buyerDetails.getAddress(),
                buyerDetails.getCompany()
        );

        if (result > 0) {
            // Set the IDs back to the details object
            buyerDetails.setBuyerId(buyerId);
            buyerDetails.setEmail(email);
            return buyerDetails;
        } else {
            throw new RuntimeException("Failed to update buyer");
        }
    }

    // Update only buyer phone
    @Transactional
    public boolean updateBuyerPhone(String buyerId, String email, String newPhone) {
        // Check if buyer exists
        int exists = buyerRepository.checkBuyerExists(buyerId, email);
        if (exists == 0) {
            throw new RuntimeException("Buyer not found with ID: " + buyerId + " and email: " + email);
        }

        // Use manual update query for phone only
        int result = buyerRepository.updateBuyerPhone(buyerId, email, newPhone);
        return result > 0;
    }

    // Update only buyer address
    @Transactional
    public boolean updateBuyerAddress(String buyerId, String email, String newAddress) {
        // Check if buyer exists
        int exists = buyerRepository.checkBuyerExists(buyerId, email);
        if (exists == 0) {
            throw new RuntimeException("Buyer not found with ID: " + buyerId + " and email: " + email);
        }

        // Use manual update query for address only
        int result = buyerRepository.updateBuyerAddress(buyerId, email, newAddress);
        return result > 0;
    }

    // Delete buyer using manual delete
    @Transactional
    public void deleteBuyer(String buyerId, String email) {
        // Check if buyer exists
        int exists = buyerRepository.checkBuyerExists(buyerId, email);
        if (exists == 0) {
            throw new RuntimeException("Buyer not found with ID: " + buyerId + " and email: " + email);
        }

        // Use manual delete query
        int result = buyerRepository.deleteBuyer(buyerId, email);
        if (result == 0) {
            throw new RuntimeException("Failed to delete buyer");
        }
        System.out.println("Buyer deleted successfully: " + buyerId);
    }

    // Search buyers by name
    public List<Buyer> searchBuyersByName(String customerName) {
        return buyerRepository.findByCustomerNameContaining(customerName);
    }

    // Get buyers by company
    public List<Buyer> getBuyersByCompany(String company) {
        return buyerRepository.findByCompany(company);
    }

    // Check if email is already registered
    public boolean isEmailRegistered(String email) {
        return buyerRepository.checkEmailExists(email) > 0;
    }

    // Check if buyer exists
    public boolean buyerExists(String buyerId, String email) {
        return buyerRepository.checkBuyerExists(buyerId, email) > 0;
    }
}