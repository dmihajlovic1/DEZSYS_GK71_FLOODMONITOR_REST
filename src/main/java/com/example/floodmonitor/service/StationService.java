package com.example.floodmonitor.service;

import com.example.floodmonitor.exception.NotFoundException;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.StationStatus;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class StationService {

    /** Ältere Messungen gelten als nicht mehr aktuell -> UNKNOWN */
    private static final Duration MAX_AGE = Duration.ofMinutes(5);

    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;

    public StationService(StationRepository stationRepository, MeasurementRepository measurementRepository) {
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
    }

    /** Alle Stationen, jeder Filter ist optional (null = nicht filtern). */
    public List<Station> findStations(String river, Boolean isActive, WarningLevel warningLevel) {
        return stationRepository.findAll().stream()
                .peek(this::updateWarningLevel)
                .filter(s -> river == null || s.getRiver().equalsIgnoreCase(river.trim()))
                .filter(s -> isActive == null || s.isActive() == isActive)
                .filter(s -> warningLevel == null || s.getWarningLevel() == warningLevel)
                .toList();
    }

    public Station getStation(String id) {
        Station station = requireStation(id);
        updateWarningLevel(station);
        return station;
    }

    /** Station laden oder 404. */
    public Station requireStation(String id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Station " + id + " wurde nicht gefunden"));
    }

    /** Aktuelle Warnstufe = Warnstufe des letzten Messwerts (wenn aktuell und gültig). */
    private void updateWarningLevel(Station s) {
        Measurement latest = measurementRepository.findLatest(s.getId()).orElse(null);
        boolean noCurrentData = !s.isActive()
                || s.getStatus() == StationStatus.OFFLINE
                || latest == null
                || latest.getTimestamp().isBefore(Instant.now().minus(MAX_AGE));
        s.setWarningLevel(noCurrentData ? WarningLevel.UNKNOWN : latest.getWarningLevel());
    }
}