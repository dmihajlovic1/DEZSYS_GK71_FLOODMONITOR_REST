package com.example.floodmonitor.dto;

import com.example.floodmonitor.model.WarningLevel;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.Instant;

@JacksonXmlRootElement(localName = "alert")
public class Alert {

    private final String stationId;
    private final String stationName;
    private final String river;
    private final WarningLevel warningLevel;
    private final double waterLevel;
    private final Instant timestamp;
    private final String message;

    public Alert(String stationId, String stationName, String river, WarningLevel warningLevel,
                 double waterLevel, Instant timestamp, String message) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.river = river;
        this.warningLevel = warningLevel;
        this.waterLevel = waterLevel;
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getStationId() { return stationId; }
    public String getStationName() { return stationName; }
    public String getRiver() { return river; }
    public WarningLevel getWarningLevel() { return warningLevel; }
    public double getWaterLevel() { return waterLevel; }
    public Instant getTimestamp() { return timestamp; }
    public String getMessage() { return message; }
}