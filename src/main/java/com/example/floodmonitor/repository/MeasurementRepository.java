package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Measurement;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MeasurementRepository {

    // Station-ID -> Liste der Messungen (älteste zuerst)
    private final Map<String, List<Measurement>> measurements = new ConcurrentHashMap<>();

    public synchronized void add(Measurement m) {
        measurements.computeIfAbsent(m.getStationId(), k -> new ArrayList<>()).add(m);
    }

    public synchronized List<Measurement> findByStationId(String stationId) {
        return new ArrayList<>(measurements.getOrDefault(stationId, new ArrayList<>()));
    }

    public synchronized Optional<Measurement> findLatest(String stationId) {
        List<Measurement> list = measurements.get(stationId);
        if (list == null || list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(list.size() - 1));
    }
}