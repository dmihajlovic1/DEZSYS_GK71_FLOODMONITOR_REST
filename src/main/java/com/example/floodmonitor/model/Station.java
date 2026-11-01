package com.example.floodmonitor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Station {

    private String id;                  // z. B. "ST-001"
    private String name;                // z. B. "Donaubruecke Linz"
    private String river;               // z. B. "Donau"
    private Location location;          // Breiten-/Längengrad
    private double normalWaterLevel;    // cm
    private double warningWaterLevel;   // cm, ab hier WARNING
    private double criticalWaterLevel;  // cm, ab hier CRITICAL

    @JsonProperty("isActive")
    private boolean isActive;

    private StationStatus status;       // ONLINE, MAINTENANCE, OFFLINE
    private WarningLevel warningLevel;  // aktuelle Warnstufe, wird vom Server berechnet

    public Station() {}

    public Station(String id, String name, String river, Location location,
                   double normalWaterLevel, double warningWaterLevel, double criticalWaterLevel,
                   boolean isActive, StationStatus status) {
        this.id = id;
        this.name = name;
        this.river = river;
        this.location = location;
        this.normalWaterLevel = normalWaterLevel;
        this.warningWaterLevel = warningWaterLevel;
        this.criticalWaterLevel = criticalWaterLevel;
        this.isActive = isActive;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRiver() { return river; }
    public void setRiver(String river) { this.river = river; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public double getNormalWaterLevel() { return normalWaterLevel; }
    public void setNormalWaterLevel(double normalWaterLevel) { this.normalWaterLevel = normalWaterLevel; }

    public double getWarningWaterLevel() { return warningWaterLevel; }
    public void setWarningWaterLevel(double warningWaterLevel) { this.warningWaterLevel = warningWaterLevel; }

    public double getCriticalWaterLevel() { return criticalWaterLevel; }
    public void setCriticalWaterLevel(double criticalWaterLevel) { this.criticalWaterLevel = criticalWaterLevel; }

    @JsonProperty("isActive")
    public boolean isActive() { return isActive; }

    @JsonProperty("isActive")
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public StationStatus getStatus() { return status; }
    public void setStatus(StationStatus status) { this.status = status; }

    public WarningLevel getWarningLevel() { return warningLevel; }
    public void setWarningLevel(WarningLevel warningLevel) { this.warningLevel = warningLevel; }
}