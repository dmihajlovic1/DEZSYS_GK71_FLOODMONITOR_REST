package com.example.floodmonitor.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "statistics")
public class StatisticsResponse {

    private final String stationId;
    private final int measurementCount;
    private final double minWaterLevel;
    private final double maxWaterLevel;
    private final double avgWaterLevel;
    private final double avgFlowRate;
    private final double totalRainfall;
    private final int warningCount;
    private final int criticalCount;

    public StatisticsResponse(String stationId, int measurementCount, double minWaterLevel,
                              double maxWaterLevel, double avgWaterLevel, double avgFlowRate,
                              double totalRainfall, int warningCount, int criticalCount) {
        this.stationId = stationId;
        this.measurementCount = measurementCount;
        this.minWaterLevel = minWaterLevel;
        this.maxWaterLevel = maxWaterLevel;
        this.avgWaterLevel = avgWaterLevel;
        this.avgFlowRate = avgFlowRate;
        this.totalRainfall = totalRainfall;
        this.warningCount = warningCount;
        this.criticalCount = criticalCount;
    }

    public String getStationId() { return stationId; }
    public int getMeasurementCount() { return measurementCount; }
    public double getMinWaterLevel() { return minWaterLevel; }
    public double getMaxWaterLevel() { return maxWaterLevel; }
    public double getAvgWaterLevel() { return avgWaterLevel; }
    public double getAvgFlowRate() { return avgFlowRate; }
    public double getTotalRainfall() { return totalRainfall; }
    public int getWarningCount() { return warningCount; }
    public int getCriticalCount() { return criticalCount; }
}