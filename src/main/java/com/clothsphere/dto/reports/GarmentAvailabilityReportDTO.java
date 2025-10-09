package com.clothsphere.dto.reports;

import java.util.List;

public class GarmentAvailabilityReportDTO {
    private String generatedDate;
    private int totalGarments;
    private List<GarmentAvailabilityItemDTO> data;

    // Getters & Setters
    public String getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(String generatedDate) { this.generatedDate = generatedDate; }

    public int getTotalGarments() { return totalGarments; }
    public void setTotalGarments(int totalGarments) { this.totalGarments = totalGarments; }

    public List<GarmentAvailabilityItemDTO> getData() { return data; }
    public void setData(List<GarmentAvailabilityItemDTO> data) { this.data = data; }
}
