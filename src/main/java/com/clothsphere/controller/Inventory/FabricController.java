package com.clothsphere.controller.Inventory;

import com.clothsphere.dto.FabricDTO;
import com.clothsphere.dto.FabricMovementDTO;
import com.clothsphere.service.Inventory.FabricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/fabrics")
public class FabricController {

    @Autowired
    private FabricService fabricService;

    // ------------------- Fabric Endpoints -------------------

    // Get all fabrics
    @GetMapping
    public List<FabricDTO> getAllFabrics() {
        return fabricService.getAllFabrics();
    }

    // Get a single fabric by ID
    @GetMapping("/{fabricId}")
    public FabricDTO getFabricById(@PathVariable String fabricId) {
        return fabricService.getFabricById(fabricId);
    }

    // Add new fabric
    @PostMapping("/new")
    public boolean addFabric(@RequestBody FabricDTO fabricDTO, @RequestParam double initialStock) {
        return fabricService.addFabric(fabricDTO, initialStock);
    }

    // Update existing fabric
    @PutMapping("/update")
    public boolean updateFabric(@RequestParam String fabricId,
                                @RequestParam String fabricType,
                                @RequestParam String color,
                                @RequestParam double currentStock) {
        FabricDTO dto = new FabricDTO(fabricId, fabricType, color, currentStock);
        return fabricService.updateFabric(dto);
    }

    // Delete fabric
    @DeleteMapping("/delete/{fabricId}")
    public boolean deleteFabric(@PathVariable String fabricId) {
        return fabricService.deleteFabric(fabricId);
    }

    // ------------------- Fabric Movements Endpoints -------------------

    // Get all movements
    @GetMapping("/movements")
    public List<FabricMovementDTO> getAllMovements() {
        return fabricService.getAllMovements();
    }

    // Get movements for a specific fabric
    @GetMapping("/movements/{fabricId}")
    public List<FabricMovementDTO> getMovementsByFabric(@PathVariable String fabricId) {
        return fabricService.getMovementsByFabric(fabricId);
    }

    // Add new movement
    @PostMapping("/movements/add")
    public boolean addMovement(@RequestParam String movementId,
                               @RequestParam String fabricId,
                               @RequestParam String status,
                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate movementDate,
                               @RequestParam double quantity) {

        FabricMovementDTO dto = new FabricMovementDTO();
        dto.setMovementId(movementId);
        dto.setFabricId(fabricId);
        dto.setStatus(status);
        dto.setMovementDate(movementDate != null ? movementDate : LocalDate.now());
        dto.setQuantity(quantity);

        return fabricService.addMovement(dto);
    }

    // Approve/Reject movement
    @PostMapping("/movements/approve")
    public boolean approveMovement(@RequestParam String movementId,
                                   @RequestParam String fabricId,
                                   @RequestParam Double approvedQuantity,
                                   @RequestParam(required = false) Double rejectedQuantity,
                                   @RequestParam String approvalStatus,
                                   @RequestParam(required = false) String rejectionReason) {

        FabricMovementDTO dto = new FabricMovementDTO();
        dto.setMovementId(movementId);
        dto.setFabricId(fabricId);
        dto.setApprovedQuantity(approvedQuantity); // fixed typo
        dto.setRejectedQuantity(rejectedQuantity != null ? rejectedQuantity : 0.0);
        dto.setApprovalStatus(approvalStatus);
        dto.setRejectionReason(rejectionReason);

        return fabricService.approveMovement(dto);
    }


}
