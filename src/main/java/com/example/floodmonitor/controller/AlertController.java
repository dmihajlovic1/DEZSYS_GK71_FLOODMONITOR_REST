package com.example.floodmonitor.controller;

import com.example.floodmonitor.dto.AlertListResponse;
import com.example.floodmonitor.dto.ApiError;
import com.example.floodmonitor.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/alerts", produces = {"application/json", "application/xml"})
@Tag(name = "Warnungen", description = "Aktuelle Hochwasserwarnungen")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    @Operation(summary = "Aktuelle Warnungen abrufen",
            description = "Alle Stationen, deren aktuelle Warnstufe WARNING oder CRITICAL ist.")
    @ApiResponse(responseCode = "200", description = "Liste der Warnungen")
    @ApiResponse(responseCode = "400", description = "Ungültige Warnstufe",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public AlertListResponse getAlerts(
            @Parameter(description = "Nur diese Warnstufe",
                    schema = @Schema(allowableValues = {"WARNING", "CRITICAL"}))
            @RequestParam(required = false) String warningLevel,
            @Parameter(description = "Nur dieser Fluss", example = "Donau")
            @RequestParam(required = false) String river) {
        return new AlertListResponse(alertService.getAlerts(QueryParams.parseWarningLevel(warningLevel), river));
    }
}