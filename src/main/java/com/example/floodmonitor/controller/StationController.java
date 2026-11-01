package com.example.floodmonitor.controller;

import com.example.floodmonitor.dto.ApiError;
import com.example.floodmonitor.dto.MeasurementListResponse;
import com.example.floodmonitor.dto.StationListResponse;
import com.example.floodmonitor.dto.StatisticsResponse;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.service.MeasurementService;
import com.example.floodmonitor.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping(value = "/api/v1/stations", produces = {"application/json", "application/xml"})
@Tag(name = "Stationen", description = "Messstationen, Messwerte und Statistik")
public class StationController {

    private final StationService stationService;
    private final MeasurementService measurementService;

    public StationController(StationService stationService, MeasurementService measurementService) {
        this.stationService = stationService;
        this.measurementService = measurementService;
    }

    @GetMapping
    @Operation(summary = "Alle Messstationen abrufen", description = "Alle Filter sind optional und kombinierbar.")
    @ApiResponse(responseCode = "200", description = "Liste der Stationen")
    @ApiResponse(responseCode = "400", description = "Ungültiger Filterwert",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "406", description = "Format nicht unterstützt",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public StationListResponse getStations(
            @Parameter(description = "Fluss (Groß-/Kleinschreibung egal)", example = "Donau")
            @RequestParam(required = false) String river,
            @Parameter(description = "Nur aktive/inaktive Stationen", schema = @Schema(type = "boolean"))
            @RequestParam(required = false) String isActive,
            @Parameter(description = "Aktuelle Warnstufe",
                    schema = @Schema(allowableValues = {"NORMAL", "WARNING", "CRITICAL", "UNKNOWN"}))
            @RequestParam(required = false) String warningLevel) {
        return new StationListResponse(stationService.findStations(
                river,
                QueryParams.parseBoolean("isActive", isActive),
                QueryParams.parseWarningLevel(warningLevel)));
    }

    @GetMapping("/{stationId}")
    @Operation(summary = "Einzelne Station abrufen")
    @ApiResponse(responseCode = "200", description = "Station gefunden")
    @ApiResponse(responseCode = "404", description = "Station nicht gefunden",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public Station getStation(@Parameter(example = "ST-001") @PathVariable String stationId) {
        return stationService.getStation(stationId);
    }

    @GetMapping("/{stationId}/measurements/latest")
    @Operation(summary = "Aktuellsten Messwert abrufen")
    @ApiResponse(responseCode = "200", description = "Aktuellster Messwert")
    @ApiResponse(responseCode = "404", description = "Station unbekannt oder noch keine Messung",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public Measurement getLatest(@Parameter(example = "ST-001") @PathVariable String stationId) {
        return measurementService.getLatest(stationId);
    }

    @GetMapping("/{stationId}/measurements")
    @Operation(summary = "Historische Messwerte abrufen", description = "Sortiert nach Zeitpunkt, neueste zuerst.")
    @ApiResponse(responseCode = "200", description = "Liste der Messwerte")
    @ApiResponse(responseCode = "400", description = "Ungültiges Datum, from nach to, ungültiges Limit oder Warnstufe",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Station nicht gefunden",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public MeasurementListResponse getMeasurements(
            @Parameter(example = "ST-001") @PathVariable String stationId,
            @Parameter(description = "Beginn (ISO-8601, inklusive)", example = "2026-05-01T08:00:00Z")
            @RequestParam(required = false) String from,
            @Parameter(description = "Ende (ISO-8601, inklusive)", example = "2026-05-01T12:00:00Z")
            @RequestParam(required = false) String to,
            @Parameter(description = "Maximale Anzahl (1–1000)",
                    schema = @Schema(type = "integer", defaultValue = "100", minimum = "1", maximum = "1000"))
            @RequestParam(required = false) String limit,
            @Parameter(description = "Nur Messwerte mit dieser Warnstufe",
                    schema = @Schema(allowableValues = {"NORMAL", "WARNING", "CRITICAL", "UNKNOWN"}))
            @RequestParam(required = false) String warningLevel) {
        Instant fromTime = QueryParams.parseInstant("from", from);
        Instant toTime = QueryParams.parseInstant("to", to);
        QueryParams.checkRange(fromTime, toTime);
        return new MeasurementListResponse(stationId, measurementService.getHistory(
                stationId, fromTime, toTime, QueryParams.parseLimit(limit), QueryParams.parseWarningLevel(warningLevel)));
    }

    @GetMapping("/{stationId}/statistics")
    @Operation(summary = "Statistik der Messwerte",
            description = "Min/Max/Durchschnitt Wasserstand, Ø Durchfluss, Summe Niederschlag, Anzahl Messungen und Warnungen.")
    @ApiResponse(responseCode = "200", description = "Statistik")
    @ApiResponse(responseCode = "400", description = "Ungültiger Zeitraum",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Station nicht gefunden",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public StatisticsResponse getStatistics(
            @Parameter(example = "ST-001") @PathVariable String stationId,
            @Parameter(description = "Beginn (ISO-8601)") @RequestParam(required = false) String from,
            @Parameter(description = "Ende (ISO-8601)") @RequestParam(required = false) String to) {
        Instant fromTime = QueryParams.parseInstant("from", from);
        Instant toTime = QueryParams.parseInstant("to", to);
        QueryParams.checkRange(fromTime, toTime);
        return measurementService.getStatistics(stationId, fromTime, toTime);
    }
}