package com.clothsphere.service.Inventory;

import com.clothsphere.dto.FabricDTO;
import com.clothsphere.dto.FabricMovementDTO;
import com.clothsphere.model.Inventory.Fabric;
import com.clothsphere.model.Inventory.FabricMovement;
import com.clothsphere.observer.Fabric.LowStockAlert;
import com.clothsphere.service.Inventory.FabricStock;
import com.clothsphere.repository.Inventory.FabricMovementRepository;
import com.clothsphere.repository.Inventory.FabricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FabricService {

    @Autowired
    private FabricRepository fabricRepository;

    @Autowired
    private FabricMovementRepository movementRepository;

    // Observable for low stock
    private final FabricStock fabricStock = new FabricStock();

    // Constructor to attach observer
    public FabricService() {
        // Example threshold: 50 meters
        fabricStock.addObserver(new LowStockAlert(50.0));
    }

    // ------------------- Fabric -------------------

    public List<FabricDTO> getAllFabrics() {
        List<Fabric> fabrics = fabricRepository.getAllFabrics();
        return fabrics.stream()
                .map(f -> new FabricDTO(f.getFabricId(), f.getFabricType(), f.getColor(), f.getCurrentStock()))
                .collect(Collectors.toList());
    }

    public FabricDTO getFabricById(String fabricId) {
        Fabric f = fabricRepository.getFabricById(fabricId);
        return new FabricDTO(f.getFabricId(), f.getFabricType(), f.getColor(), f.getCurrentStock());
    }

    public boolean addFabric(FabricDTO fabricDTO, double initialStock) {
        Fabric fabric = new Fabric(fabricDTO.getFabricId(), fabricDTO.getFabricType(),
                fabricDTO.getColor(), initialStock);
        boolean result = fabricRepository.addFabric(fabric) > 0;

        // Notify observer after adding new fabric
        if (result) {
            fabricStock.checkStock(fabric.getFabricId(), initialStock);
        }
        return result;
    }

    public boolean updateFabric(FabricDTO fabricDTO) {
        Fabric fabric = new Fabric();
        fabric.setFabricId(fabricDTO.getFabricId());
        fabric.setFabricType(fabricDTO.getFabricType());
        fabric.setColor(fabricDTO.getColor());
        fabric.setCurrentStock(fabricDTO.getCurrentStock());

        boolean result = fabricRepository.updateFabric(fabric) > 0;

        // Notify observer after stock update
        if (result) {
            fabricStock.checkStock(fabric.getFabricId(), fabric.getCurrentStock());
        }
        return result;
    }

    public boolean deleteFabric(String fabricId) {
        return fabricRepository.deleteFabric(fabricId) > 0;
    }

    // ------------------- Fabric Movements -------------------

    public List<FabricMovementDTO> getAllMovements() {
        List<FabricMovement> movements = movementRepository.getAllMovements();
        return movements.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<FabricMovementDTO> getMovementsByFabric(String fabricId) {
        List<FabricMovement> movements = movementRepository.getMovementsByFabricId(fabricId);
        return movements.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private String generateMovementId() {
        // Implement your ID generation logic
        return "MOV" + System.currentTimeMillis(); // Example
    }

    public boolean addMovement(FabricMovementDTO dto) {
        Fabric fabric = fabricRepository.getFabricById(dto.getFabricId());

        double totalStock = fabric.getCurrentStock();
        if ("IN".equalsIgnoreCase(dto.getStatus())) {
            totalStock += dto.getQuantity();
        } else if ("OUT".equalsIgnoreCase(dto.getStatus())) {
            totalStock -= dto.getQuantity();
        }

        FabricMovement movement = new FabricMovement(
                dto.getMovementId(),
                dto.getFabricId(),
                dto.getStatus(),
                dto.getMovementDate() != null ? dto.getMovementDate() : LocalDate.now(),
                dto.getQuantity(),
                totalStock
        );

        int result = movementRepository.addMovement(movement);
        return result > 0;
    }

    // Approve or reject a movement
    public boolean approveMovement(FabricMovementDTO dto) {
        FabricMovement movement = movementRepository.getMovementsByFabricId(dto.getFabricId()).stream()
                .filter(m -> m.getMovementId().equals(dto.getMovementId()))
                .findFirst().orElse(null);

        if (movement == null) return false;

        movement.setApprovedQuantity(dto.getApprovedQuantity());
        movement.setRejectedQuantity(dto.getRejectedQuantity());
        movement.setApprovalStatus(dto.getApprovalStatus());
        movement.setRejectionReason(dto.getRejectionReason());

        Fabric fabric = fabricRepository.getFabricById(movement.getFabricId());
        double newStock = fabric.getCurrentStock();

        Double approvedQty = dto.getApprovedQuantity();
        double qty = approvedQty != null ? approvedQty : 0.0;

        if ("IN".equalsIgnoreCase(movement.getStatus())) {
            newStock += qty;
        } else if ("OUT".equalsIgnoreCase(movement.getStatus()) || "SPECIAL RELEASE".equalsIgnoreCase(movement.getStatus())) {
            newStock -= qty;
        }

        fabric.setCurrentStock(newStock);

        int updatedMovement = movementRepository.updateMovement(movement);
        int updatedFabric = fabricRepository.updateFabric(fabric);

        // Notify observer after stock update
        if (updatedFabric > 0) {
            fabricStock.checkStock(fabric.getFabricId(), newStock);
        }

        return updatedMovement > 0 && updatedFabric > 0;
    }

    // Helper: Convert model → DTO
    private FabricMovementDTO toDTO(FabricMovement m) {
        return new FabricMovementDTO(
                m.getMovementId(),
                m.getFabricId(),
                m.getStatus(),
                m.getMovementDate(),
                m.getQuantity(),
                m.getTotalStock(),
                m.getApprovedQuantity(),
                m.getRejectedQuantity(),
                m.getApprovalStatus(),
                m.getRejectionReason()
        );
    }

    // Get current total quantity for a fabric
    public double getCurrentTotalQuantity(String fabricId) {
        try {
            Fabric fabric = fabricRepository.getFabricById(fabricId);
            return fabric.getCurrentStock();
        } catch (Exception e) {
            return 0.0;
        }
    }
}
