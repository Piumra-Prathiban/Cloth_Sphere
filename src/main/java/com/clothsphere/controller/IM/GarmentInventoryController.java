package com.clothsphere.controller.IM;

import com.clothsphere.dto.IM.GarmentCreationRequest;
import com.clothsphere.model.IM.Garment;
import com.clothsphere.service.IM.GarmentInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inventory/garments")
@CrossOrigin(origins = "*")
public class GarmentInventoryController {

    @Autowired
    private GarmentInventoryService garmentInventoryService;

    @PostMapping("/create-with-product")
    public ResponseEntity<?> createGarmentWithProduct(@RequestBody GarmentCreationRequest request) {
        try {
            Garment createdGarment = garmentInventoryService.createNewGarmentWithProduct(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGarment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create garment: " + e.getMessage()));
        }
    }

    @GetMapping("/next-garment-id")
    public ResponseEntity<String> getNextGarmentId() {
        try {
            // Use the service method instead of local calculation
            String nextId = garmentInventoryService.getNextGarmentId();
            return ResponseEntity.ok(nextId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating garment ID");
        }
    }
}