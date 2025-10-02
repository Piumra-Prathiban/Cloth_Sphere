package com.clothsphere.model.Production;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "workstation")
public class WorkStation {

    @Id
    @Column(name = "workstation_id", length = 10, nullable = false)
    private String workstationId;

    @Column(name = "workstation_name", nullable = false, length = 100)
    private String workstationName;

    @Column(name = "workstation_type", length = 50)
    private String workstationType; // CUTTING, SEWING, FINISHING, QUALITY_CHECK, PACKING

    @Column(name = "capacity", nullable = false)
    private Integer capacity; // Number of workers it can accommodate

    @Column(name = "current_load")
    private Integer currentLoad = 0; // Current number of workers assigned

    @Column(name = "status", length = 20)
    private String status; // ACTIVE, INACTIVE, MAINTENANCE

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "supervisor_id", length = 10)
    private String supervisorId;

    @Column(name = "supervisor_name", length = 100)
    private String supervisorName;

    @Column(name = "equipment_details", length = 500)
    private String equipmentDetails;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public WorkStation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.currentLoad = 0;
    }

    // Parameterized constructor
    public WorkStation(String workstationId, String workstationName, String workstationType,
                      Integer capacity, String status, String location) {
        this.workstationId = workstationId;
        this.workstationName = workstationName;
        this.workstationType = workstationType;
        this.capacity = capacity;
        this.status = status;
        this.location = location;
        this.currentLoad = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getWorkstationId() { return workstationId; }
    public void setWorkstationId(String workstationId) { this.workstationId = workstationId; }

    public String getWorkstationName() { return workstationName; }
    public void setWorkstationName(String workstationName) { this.workstationName = workstationName; }

    public String getWorkstationType() { return workstationType; }
    public void setWorkstationType(String workstationType) { this.workstationType = workstationType; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getCurrentLoad() { return currentLoad; }
    public void setCurrentLoad(Integer currentLoad) {
        this.currentLoad = currentLoad;
        this.updatedAt = LocalDateTime.now();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSupervisorId() { return supervisorId; }
    public void setSupervisorId(String supervisorId) { this.supervisorId = supervisorId; }

    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }

    public String getEquipmentDetails() { return equipmentDetails; }
    public void setEquipmentDetails(String equipmentDetails) { this.equipmentDetails = equipmentDetails; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Calculated field for utilization percentage
    @Transient
    public Double getUtilizationPercentage() {
        if (capacity == null || capacity == 0) return 0.0;
        return (currentLoad * 100.0) / capacity;
    }

    // Check if workstation has available capacity
    @Transient
    public boolean hasCapacity() {
        return currentLoad < capacity;
    }

    @Override
    public String toString() {
        return "WorkStation{" +
                "workstationId='" + workstationId + '\'' +
                ", workstationName='" + workstationName + '\'' +
                ", workstationType='" + workstationType + '\'' +
                ", capacity=" + capacity +
                ", currentLoad=" + currentLoad +
                ", status='" + status + '\'' +
                '}';
    }
}