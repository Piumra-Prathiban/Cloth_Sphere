package com.clothsphere.dto.reports;

import java.util.List;

public class FabricAvailabilityReportDTO {
    private String generatedDate;
    private int totalFabrics;
    private List<FabricAvailabilityItemDTO> data;

    // Getters & Setters
    public String getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(String generatedDate) { this.generatedDate = generatedDate; }

    public int getTotalFabrics() { return totalFabrics; }
    public void setTotalFabrics(int totalFabrics) { this.totalFabrics = totalFabrics; }

    public List<FabricAvailabilityItemDTO> getData() { return data; }
    public void setData(List<FabricAvailabilityItemDTO> data) { this.data = data; }
}
