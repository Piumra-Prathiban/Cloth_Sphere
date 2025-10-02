package com.clothsphere.repository.Inventory;

import com.clothsphere.model.Inventory.Fabric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FabricRepository extends JpaRepository<Fabric, String> {

    // Find fabrics by type
    List<Fabric> findByFabricType(String fabricType);

    // Find fabrics by color
    List<Fabric> findByColor(String color);

    // Find fabrics by type and color (returns LIST now, not single item)
    List<Fabric> findByFabricTypeAndColor(String fabricType, String color);

    // Check if fabric ID exists
    boolean existsByFabricId(String fabricId);

    // Get all distinct fabric types
    @Query("SELECT DISTINCT f.fabricType FROM Fabric f")
    List<String> findDistinctFabricTypes();
}