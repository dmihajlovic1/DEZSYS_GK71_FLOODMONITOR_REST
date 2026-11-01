package com.example.floodmonitor.service;

import com.example.floodmonitor.dto.Alert;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlertService {

    private final StationService stationService;
    private final MeasurementRepository measurementRepository;

    public AlertService(StationService stationService, MeasurementRepository measurementRepository) {
        this.stationService = stationService;
        this.measurementRepository = measurementRepository;
    }

    /** Alle Stationen mit aktueller Warnstufe WARNING oder CRITICAL. */
    public List<Alert> getAlerts(WarningLevel warningLevel, String river) {
        List<Alert> alerts = new ArrayList<>();
        for (Station s : stationService.findStations(river, null, null)) {
            WarningLevel level = s.getWarningLevel();
            if (level != WarningLevel.WARNING && level != WarningLevel.CRITICAL) continue;
            if (warningLevel != null && level != warningLevel) continue;

            Measurement m = measurementRepository.findLatest(s.getId()).orElse(null);
            if (m == null) continue;

            String text = (level == WarningLevel.CRITICAL ? "Kritischer" : "Erhöhter")
                    + " Wasserstand an " + s.getName() + " (" + s.getRiver() + "): " + m.getWaterLevel() + " cm";
            alerts.add(new Alert(s.getId(), s.getName(), s.getRiver(), level, m.getWaterLevel(), m.getTimestamp(), text));
        }
        return alerts;
    }
}