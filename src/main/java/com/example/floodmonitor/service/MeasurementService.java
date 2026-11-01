package com.example.floodmonitor.service;

import com.example.floodmonitor.dto.StatisticsResponse;
import com.example.floodmonitor.exception.NotFoundException;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;

@Service
public class MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final StationService stationService;

    public MeasurementService(MeasurementRepository measurementRepository, StationService stationService) {
        this.measurementRepository = measurementRepository;
        this.stationService = stationService;
    }

    public Measurement getLatest(String stationId) {
        stationService.requireStation(stationId); // 404, wenn Station unbekannt
        return measurementRepository.findLatest(stationId)
                .orElseThrow(() -> new NotFoundException("Für Station " + stationId + " liegt noch keine Messung vor"));
    }

    /** Historische Messwerte, neueste zuerst. */
    public List<Measurement> getHistory(String stationId, Instant from, Instant to, int limit, WarningLevel warningLevel) {
        stationService.requireStation(stationId);
        return measurementRepository.findByStationId(stationId).stream()
                .filter(m -> inRange(m, from, to))
                .filter(m -> warningLevel == null || m.getWarningLevel() == warningLevel)
                .sorted(Comparator.comparing(Measurement::getTimestamp).reversed())
                .limit(limit)
                .toList();
    }

    public StatisticsResponse getStatistics(String stationId, Instant from, Instant to) {
        stationService.requireStation(stationId);
        List<Measurement> list = measurementRepository.findByStationId(stationId).stream()
                .filter(m -> inRange(m, from, to))
                .toList();
        if (list.isEmpty()) {
            return new StatisticsResponse(stationId, 0, 0, 0, 0, 0, 0, 0, 0);
        }
        DoubleSummaryStatistics level = list.stream().mapToDouble(Measurement::getWaterLevel).summaryStatistics();
        double avgFlow = list.stream().mapToDouble(Measurement::getFlowRate).average().orElse(0);
        double rainSum = list.stream().mapToDouble(Measurement::getRainfall).sum();
        int warnings = (int) list.stream().filter(m -> m.getWarningLevel() == WarningLevel.WARNING).count();
        int criticals = (int) list.stream().filter(m -> m.getWarningLevel() == WarningLevel.CRITICAL).count();

        return new StatisticsResponse(stationId, list.size(), round(level.getMin()), round(level.getMax()),
                round(level.getAverage()), round(avgFlow), round(rainSum), warnings, criticals);
    }

    private boolean inRange(Measurement m, Instant from, Instant to) {
        return (from == null || !m.getTimestamp().isBefore(from))
                && (to == null || !m.getTimestamp().isAfter(to));
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}