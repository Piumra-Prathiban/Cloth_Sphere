package com.clothsphere.dto.reports;

import java.util.List;

public class FabricUsageReportDTO {
    private String startDate;
    private String endDate;
    private String generatedDate;
    private int totalRecords;
    private List<FabricUsageItemDTO> data;

    // Getters & Setters
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(String generatedDate) { this.generatedDate = generatedDate; }

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public List<FabricUsageItemDTO> getData() { return data; }
    public void setData(List<FabricUsageItemDTO> data) { this.data = data; }
}

