package com.example.floodmonitor.controller;

import com.example.floodmonitor.dto.MeasurementListResponse;
import com.example.floodmonitor.dto.StationListResponse;
import com.example.floodmonitor.dto.StatisticsResponse;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.service.MeasurementService;
import com.example.floodmonitor.service.StationService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/stations")
public class StationController {

    private final StationService stationService;
    private final MeasurementService measurementService;

    public StationController(StationService stationService, MeasurementService measurementService) {
        this.stationService = stationService;
        this.measurementService = measurementService;
    }

    // GET /api/v1/stations?river=Donau&isActive=true&warningLevel=CRITICAL
    @GetMapping
    public StationListResponse getStations(@RequestParam(required = false) String river,
                                           @RequestParam(required = false) String isActive,
                                           @RequestParam(required = false) String warningLevel) {
        return new StationListResponse(stationService.findStations(
                river,
                QueryParams.parseBoolean("isActive", isActive),
                QueryParams.parseWarningLevel(warningLevel)));
    }

    // GET /api/v1/stations/ST-001
    @GetMapping("/{stationId}")
    public Station getStation(@PathVariable String stationId) {
        return stationService.getStation(stationId);
    }

    // GET /api/v1/stations/ST-001/measurements/latest
    @GetMapping("/{stationId}/measurements/latest")
    public Measurement getLatest(@PathVariable String stationId) {
        return measurementService.getLatest(stationId);
    }

    // GET /api/v1/stations/ST-001/measurements?from=...&to=...&limit=100&warningLevel=WARNING
    @GetMapping("/{stationId}/measurements")
    public MeasurementListResponse getMeasurements(@PathVariable String stationId,
                                                   @RequestParam(required = false) String from,
                                                   @RequestParam(required = false) String to,
                                                   @RequestParam(required = false) String limit,
                                                   @RequestParam(required = false) String warningLevel) {
        Instant fromTime = QueryParams.parseInstant("from", from);
        Instant toTime = QueryParams.parseInstant("to", to);
        QueryParams.checkRange(fromTime, toTime);
        return new MeasurementListResponse(stationId, measurementService.getHistory(
                stationId, fromTime, toTime, QueryParams.parseLimit(limit), QueryParams.parseWarningLevel(warningLevel)));
    }

    // GET /api/v1/stations/ST-001/statistics?from=...&to=...
    @GetMapping("/{stationId}/statistics")
    public StatisticsResponse getStatistics(@PathVariable String stationId,
                                            @RequestParam(required = false) String from,
                                            @RequestParam(required = false) String to) {
        Instant fromTime = QueryParams.parseInstant("from", from);
        Instant toTime = QueryParams.parseInstant("to", to);
        QueryParams.checkRange(fromTime, toTime);
        return measurementService.getStatistics(stationId, fromTime, toTime);
    }
}