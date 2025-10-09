package com.clothsphere.repository.Inventory;

import com.clothsphere.model.Inventory.Garment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarmentRepository extends JpaRepository<Garment, String> {
}

// ==========================================



import com.clothsphere.model.Inventory.Fabric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FabricRepository extends JpaRepository<Fabric, String> {
}

// ==========================================



import com.clothsphere.model.Inventory.GarmentMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GarmentMovementRepository extends JpaRepository<GarmentMovement, String> {

    @Query("SELECT gm.status, SUM(gm.quantity) FROM GarmentMovement gm " +
            "WHERE gm.garmentId = :garmentId " +
            "AND gm.movementDate BETWEEN :startDate AND :endDate " +
            "GROUP BY gm.status")
    List<Object[]> findMovementsByGarmentAndDateRange(
            @Param("garmentId") String garmentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}

// ==========================================



import com.clothsphere.model.Inventory.FabricMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FabricMovementRepository extends JpaRepository<FabricMovement, String> {

    @Query("SELECT fm.status, SUM(fm.quantity) FROM FabricMovement fm " +
            "WHERE fm.fabricId = :fabricId " +
            "AND fm.movementDate BETWEEN :startDate AND :endDate " +
            "GROUP BY fm.status")
    List<Object[]> findMovementsByFabricAndDateRange(
            @Param("fabricId") String fabricId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}