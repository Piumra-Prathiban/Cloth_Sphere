package com.clothsphere.repository.Production;

import com.clothsphere.model.Production.WorkStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface WorkStationRepository extends JpaRepository<WorkStation, String> {

    // ===================== MANUAL INSERT/UPDATE/DELETE =====================

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO workstation (workstation_id, workstation_name, workstation_type, capacity, " +
            "current_load, status, location, supervisor_id, supervisor_name, equipment_details, " +
            "created_at, updated_at) VALUES (:workstationId, :workstationName, :workstationType, " +
            ":capacity, :currentLoad, :status, :location, :supervisorId, :supervisorName, " +
            ":equipmentDetails, NOW(), NOW())", nativeQuery = true)
    void insertWorkStation(@Param("workstationId") String workstationId,
                           @Param("workstationName") String workstationName,
                           @Param("workstationType") String workstationType,
                           @Param("capacity") Integer capacity,
                           @Param("currentLoad") Integer currentLoad,
                           @Param("status") String status,
                           @Param("location") String location,
                           @Param("supervisorId") String supervisorId,
                           @Param("supervisorName") String supervisorName,
                           @Param("equipmentDetails") String equipmentDetails);

    @Modifying
    @Transactional
    @Query(value = "UPDATE workstation SET workstation_name = :workstationName, workstation_type = :workstationType, " +
            "capacity = :capacity, current_load = :currentLoad, status = :status, location = :location, " +
            "supervisor_id = :supervisorId, supervisor_name = :supervisorName, equipment_details = :equipmentDetails, " +
            "updated_at = NOW() WHERE workstation_id = :workstationId", nativeQuery = true)
    int updateWorkStation(@Param("workstationId") String workstationId,
                          @Param("workstationName") String workstationName,
                          @Param("workstationType") String workstationType,
                          @Param("capacity") Integer capacity,
                          @Param("currentLoad") Integer currentLoad,
                          @Param("status") String status,
                          @Param("location") String location,
                          @Param("supervisorId") String supervisorId,
                          @Param("supervisorName") String supervisorName,
                          @Param("equipmentDetails") String equipmentDetails);

    @Modifying
    @Transactional
    @Query(value = "UPDATE workstation SET status = :status, updated_at = NOW() WHERE workstation_id = :workstationId", nativeQuery = true)
    int updateWorkstationStatus(@Param("workstationId") String workstationId, @Param("status") String status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE workstation SET current_load = :currentLoad, updated_at = NOW() WHERE workstation_id = :workstationId", nativeQuery = true)
    int updateCurrentLoad(@Param("workstationId") String workstationId, @Param("currentLoad") Integer currentLoad);

    @Modifying
    @Transactional
    @Query(value = "UPDATE workstation SET supervisor_id = :supervisorId, supervisor_name = :supervisorName, updated_at = NOW() WHERE workstation_id = :workstationId", nativeQuery = true)
    int updateSupervisor(@Param("workstationId") String workstationId,
                         @Param("supervisorId") String supervisorId,
                         @Param("supervisorName") String supervisorName);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM workstation WHERE workstation_id = :workstationId", nativeQuery = true)
    int deleteWorkStation(@Param("workstationId") String workstationId);

    // ===================== EXISTING QUERY METHODS =====================

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