package com.clothsphere.service.buyerPortal;

import com.clothsphere.model.buyerPortal.Buyer;
import com.clothsphere.repository.buyerPortal.BuyerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BuyerService {

    @Autowired
    private BuyerRepository buyerRepository;

    // Register new buyer
    public Buyer register(Buyer buyer) {
        // Optional: add check if email already exists
        if(buyerRepository.findByEmail(buyer.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        return buyerRepository.save(buyer);
    }

    // Login buyer
    public Buyer login(String email, String password) {
        Optional<Buyer> buyer = buyerRepository.findByEmail(email);
        if(buyer.isPresent() && buyer.get().getPassword().equals(password)) {
            return buyer.get();
        }
        return null;
    }
    public Buyer updateBuyer(Buyer buyer) {
        return buyerRepository.save(buyer);  // save() works as update if ID exists
    }
    public Buyer getBuyerById(Long id) {
        return buyerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));
    }

}
