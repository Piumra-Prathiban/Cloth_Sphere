package com.clothsphere.repository.Inventory;

import com.clothsphere.model.Inventory.GarmentMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface GarmentMovementRepository extends JpaRepository<GarmentMovement, String> {

    // Find all movements for a specific garment
    List<GarmentMovement> findByGarmentIdOrderByMovementDateDesc(String garmentId);

    // Find movements by status
    List<GarmentMovement> findByStatus(String status);

    // Find movements by date
    List<GarmentMovement> findByMovementDate(LocalDate movementDate);

    // Find movements by date range
    List<GarmentMovement> findByMovementDateBetween(LocalDate startDate, LocalDate endDate);

    // Get latest movement for a garment (to get current total)
    @Query("SELECT gm FROM GarmentMovement gm WHERE gm.garmentId = :garmentId ORDER BY gm.movementDate DESC, gm.movementId DESC")
    List<GarmentMovement> findLatestMovementByGarmentId(@Param("garmentId") String garmentId);

    // Get stock in movements for today
    @Query("SELECT gm FROM GarmentMovement gm WHERE gm.status = 'In' AND gm.movementDate = :today")
    List<GarmentMovement> findStockInToday(@Param("today") LocalDate today);

    // Get shipped movements for today
    @Query("SELECT gm FROM GarmentMovement gm WHERE gm.status = 'Shipped' AND gm.movementDate = :today")
    List<GarmentMovement> findShippedToday(@Param("today") LocalDate today);

    // Find movements by fabric ID
    List<GarmentMovement> findByFabricId(String fabricId);

    // Check if movement ID exists
    boolean existsByMovementId(String movementId);
}