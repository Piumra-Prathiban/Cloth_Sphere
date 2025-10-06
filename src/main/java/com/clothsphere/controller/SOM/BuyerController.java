package com.clothsphere.controller.SOM;

import com.clothsphere.model.SOM.Buyer;
import com.clothsphere.service.SOM.BuyerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/buyers")
@CrossOrigin(origins = "http://localhost:8080")
public class BuyerController {

    @Autowired
    private BuyerService buyerService;

    // Create new buyer
    @PostMapping
    public ResponseEntity<?> createBuyer(@RequestBody Buyer buyer) {
        try {
            Buyer createdBuyer = buyerService.createBuyer(buyer);
            return new ResponseEntity<>(createdBuyer, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    // Get all buyers
    @GetMapping
    public ResponseEntity<List<Buyer>> getAllBuyers() {
        List<Buyer> buyers = buyerService.getAllBuyers();
        return new ResponseEntity<>(buyers, HttpStatus.OK);
    }

    // Get buyer by ID and email
    @GetMapping("/{buyerId}/{email}")
    public ResponseEntity<?> getBuyer(@PathVariable String buyerId, @PathVariable String email) {
        Optional<Buyer> buyer = buyerService.getBuyerById(buyerId, email);
        if (buyer.isPresent()) {
            return new ResponseEntity<>(buyer.get(), HttpStatus.OK);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Buyer not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    // Update buyer
    @PutMapping("/{buyerId}/{email}")
    public ResponseEntity<?> updateBuyer(@PathVariable String buyerId,
                                         @PathVariable String email,
                                         @RequestBody Buyer buyerDetails) {
        try {
            Buyer updatedBuyer = buyerService.updateBuyer(buyerId, email, buyerDetails);
            return new ResponseEntity<>(updatedBuyer, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    // Delete buyer
    @DeleteMapping("/{buyerId}/{email}")
    public ResponseEntity<?> deleteBuyer(@PathVariable String buyerId, @PathVariable String email) {
        try {
            buyerService.deleteBuyer(buyerId, email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Buyer deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    // Search buyers by name
    @GetMapping("/search")
    public ResponseEntity<List<Buyer>> searchBuyers(@RequestParam String name) {
        List<Buyer> buyers = buyerService.searchBuyersByName(name);
        return new ResponseEntity<>(buyers, HttpStatus.OK);
    }

    // Get next buyer ID
    @GetMapping("/next-id")
    public ResponseEntity<Map<String, String>> getNextBuyerId() {
        String nextId = buyerService.generateNextBuyerId();
        Map<String, String> response = new HashMap<>();
        response.put("nextBuyerId", nextId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}