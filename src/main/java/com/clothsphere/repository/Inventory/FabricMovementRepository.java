package com.clothsphere.repository.Inventory;

import com.clothsphere.model.Inventory.FabricMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FabricMovementRepository extends JpaRepository<FabricMovement, String> {

    // Find all movements for a specific fabric
    List<FabricMovement> findByFabricIdOrderByMovementDateDesc(String fabricId);

    // Find movements by status
    List<FabricMovement> findByStatus(String status);

    // Find movements by date
    List<FabricMovement> findByMovementDate(LocalDate movementDate);

    // Find movements by date range
    List<FabricMovement> findByMovementDateBetween(LocalDate startDate, LocalDate endDate);

    // Get latest movement for a fabric (to get current total)
    @Query("SELECT fm FROM FabricMovement fm WHERE fm.fabricId = :fabricId ORDER BY fm.movementDate DESC, fm.movementId DESC")
    List<FabricMovement> findLatestMovementByFabricId(@Param("fabricId") String fabricId);

    // Get stock in movements for today
    @Query("SELECT fm FROM FabricMovement fm WHERE fm.status = 'In' AND fm.movementDate = :today")
    List<FabricMovement> findStockInToday(@Param("today") LocalDate today);

    // Get stock out movements for today
    @Query("SELECT fm FROM FabricMovement fm WHERE fm.status = 'Out' AND fm.movementDate = :today")
    List<FabricMovement> findStockOutToday(@Param("today") LocalDate today);

    // Check if movement ID exists
    boolean existsByMovementId(String movementId);
}