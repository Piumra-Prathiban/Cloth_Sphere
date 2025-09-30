package com.clothsphere.repository.Inventory;

import com.clothsphere.model.Inventory.Garment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GarmentRepository extends JpaRepository<Garment, String> {

    // Find garment by type
    List<Garment> findByGarmentType(String garmentType);

    // Find garment by size
    List<Garment> findBySize(String size);

    // Find garment by type and size
    Garment findByGarmentTypeAndSize(String garmentType, String size);

    // Check if garment exists
    boolean existsByGarmentId(String garmentId);

    // Get all distinct garment types
    @Query("SELECT DISTINCT g.garmentType FROM Garment g")
    List<String> findDistinctGarmentTypes();

    // Get all distinct sizes
    @Query("SELECT DISTINCT g.size FROM Garment g")
    List<String> findDistinctSizes();
}