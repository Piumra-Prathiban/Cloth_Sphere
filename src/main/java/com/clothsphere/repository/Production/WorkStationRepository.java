package com.clothsphere.repository.Production;

import com.clothsphere.model.Production.WorkStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkStationRepository extends JpaRepository<WorkStation, String> {

    // Find workstations by type
    List<WorkStation> findByWorkstationType(String workstationType);

    // Find workstations by status
    List<WorkStation> findByStatus(String status);

    // Find workstations by supervisor
    List<WorkStation> findBySupervisorId(String supervisorId);

    // Find active workstations
    @Query("SELECT ws FROM WorkStation ws WHERE ws.status = 'ACTIVE'")
    List<WorkStation> findActiveWorkstations();

    // Find workstations with available capacity
    @Query("SELECT ws FROM WorkStation ws WHERE ws.currentLoad < ws.capacity AND ws.status = 'ACTIVE'")
    List<WorkStation> findWorkstationsWithCapacity();

    // Find workstations by type and status
    @Query("SELECT ws FROM WorkStation ws WHERE ws.workstationType = :type AND ws.status = :status")
    List<WorkStation> findByTypeAndStatus(@Param("type") String type,
                                          @Param("status") String status);

    // Get all workstation IDs for ID generation
    @Query("SELECT ws.workstationId FROM WorkStation ws")
    List<String> findAllWorkstationIds();

    // Count workstations by status
    @Query("SELECT COUNT(ws) FROM WorkStation ws WHERE ws.status = :status")
    Long countByStatus(@Param("status") String status);

    // Find overloaded workstations
    @Query("SELECT ws FROM WorkStation ws WHERE ws.currentLoad >= ws.capacity")
    List<WorkStation> findOverloadedWorkstations();
}