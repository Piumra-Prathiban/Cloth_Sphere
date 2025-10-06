package com.clothsphere.service.SOM;

import com.clothsphere.model.SOM.Buyer;
import com.clothsphere.repository.SOM.BuyerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    // Create new buyer
    public Buyer createBuyer(Buyer buyer) {
        // Check if buyer with same email already exists
        Optional<Buyer> existingBuyer = buyerRepository.findByEmail(buyer.getEmail());
        if (existingBuyer.isPresent()) {
            throw new RuntimeException("Buyer with email " + buyer.getEmail() + " already exists");
        }

        // Generate buyer ID if not provided
        if (buyer.getBuyerId() == null || buyer.getBuyerId().isEmpty()) {
            buyer.setBuyerId(generateNextBuyerId());
        }

        return buyerRepository.save(buyer);
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

    // Update buyer
    public Buyer updateBuyer(String buyerId, String email, Buyer buyerDetails) {
        Optional<Buyer> existingBuyer = buyerRepository.findByBuyerIdAndEmail(buyerId, email);
        if (existingBuyer.isPresent()) {
            Buyer buyer = existingBuyer.get();
            buyer.setCustomerName(buyerDetails.getCustomerName());
            buyer.setPhone(buyerDetails.getPhone());
            buyer.setAddress(buyerDetails.getAddress());
            buyer.setCompany(buyerDetails.getCompany());

            return buyerRepository.save(buyer);
        }
        throw new RuntimeException("Buyer not found with ID: " + buyerId + " and email: " + email);
    }

    // Delete buyer
    public void deleteBuyer(String buyerId, String email) {
        Optional<Buyer> buyer = buyerRepository.findByBuyerIdAndEmail(buyerId, email);
        if (buyer.isPresent()) {
            buyerRepository.delete(buyer.get());
        } else {
            throw new RuntimeException("Buyer not found with ID: " + buyerId + " and email: " + email);
        }
    }

    // Search buyers by name
    public List<Buyer> searchBuyersByName(String customerName) {
        return buyerRepository.findByCustomerNameContaining(customerName);
    }
}