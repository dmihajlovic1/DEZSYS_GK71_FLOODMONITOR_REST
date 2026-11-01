package com.example.floodmonitor.service;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.WarningLevel;
import org.springframework.stereotype.Service;

@Service
public class WarningLevelService {

    public WarningLevel calculate(Station station, Measurement m) {
        if (station == null || m == null || m.getWaterLevel() < 0) {
            return WarningLevel.UNKNOWN;
        }
        if (m.getWaterLevel() >= station.getCriticalWaterLevel()) {
            return WarningLevel.CRITICAL;
        }
        if (m.getWaterLevel() >= station.getWarningWaterLevel()) {
            return WarningLevel.WARNING;
        }
        return WarningLevel.NORMAL;
    }
}