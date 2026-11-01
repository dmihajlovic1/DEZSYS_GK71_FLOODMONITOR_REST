package com.example.floodmonitor.service;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.StationStatus;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
@ConditionalOnProperty(name = "simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationService {

    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;
    private final WarningLevelService warningLevelService;
    private final Random random = new Random();

    public SimulationService(StationRepository stationRepository,
                             MeasurementRepository measurementRepository,
                             WarningLevelService warningLevelService) {
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
        this.warningLevelService = warningLevelService;
    }

    /** Wird alle simulation.interval Millisekunden aufgerufen. */
    @Scheduled(fixedRateString = "${simulation.interval:10000}")
    public void tick() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        for (Station station : stationRepository.findAll()) {
            // Regel: deaktivierte oder ausgefallene Stationen messen nicht
            if (!station.isActive() || station.getStatus() == StationStatus.OFFLINE) {
                continue;
            }
            Measurement prev = measurementRepository.findLatest(station.getId()).orElse(null);
            Measurement next = (prev == null) ? first(station, now) : next(station, prev, now);
            next.setWarningLevel(warningLevelService.calculate(station, next));
            measurementRepository.add(next);
            station.setStatus(next.getStatus());
        }
    }

    /** Erste Messung: Wasserstand nahe dem Normalwert. */
    private Measurement first(Station s, Instant time) {
        double level = s.getNormalWaterLevel() * (0.95 + random.nextDouble() * 0.1);
        return new Measurement(s.getId(), time, round(level), round(flow(level)), 0.0,
                round(10 + random.nextDouble() * 8), 70 + random.nextInt(31), StationStatus.ONLINE, null);
    }

    /** Nächste Messung wird aus der vorherigen berechnet. */
    private Measurement next(Station s, Measurement prev, Instant time) {
        // Regen: klingt um 20 % ab, mit 8 % Wahrscheinlichkeit beginnt ein neuer Schauer
        double rain = prev.getRainfall() * 0.8;
        if (random.nextDouble() < 0.08) {
            rain += 5 + random.nextDouble() * 25;
        }
        if (rain < 0.1) rain = 0;

        // Wasserstand: Regen hebt, Rückgang Richtung Normalwert, kleines Rauschen
        double change = rain * 0.8
                - (prev.getWaterLevel() - s.getNormalWaterLevel()) * 0.03
                + random.nextGaussian();
        change = Math.max(-30, Math.min(30, change));          // max. 30 cm pro Messung
        double level = Math.max(0, prev.getWaterLevel() + change); // nie negativ

        // Temperatur: kleine Schwankung
        double temp = prev.getTemperature() + random.nextGaussian() * 0.2;

        // Akku: sinkt langsam; <= 10 % -> MAINTENANCE, dort wird geladen bis 90 %
        int battery = prev.getBatteryLevel();
        StationStatus status = prev.getStatus();
        if (status == StationStatus.MAINTENANCE) {
            battery += 8;
            if (battery >= 90) status = StationStatus.ONLINE;
        } else {
            if (random.nextDouble() < 0.1) battery--;
            if (battery <= 10) status = StationStatus.MAINTENANCE;
        }
        battery = Math.max(0, Math.min(100, battery));

        return new Measurement(s.getId(), time, round(level), round(flow(level)), round(rain),
                round(temp), battery, status, null);
    }

    /** Höherer Wasserstand -> höhere Durchflussmenge (Q ~ h^1.6). */
    private double flow(double level) {
        return 0.1 * Math.pow(level, 1.6);
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}