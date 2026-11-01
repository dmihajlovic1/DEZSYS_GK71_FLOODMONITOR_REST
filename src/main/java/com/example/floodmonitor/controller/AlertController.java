package com.example.floodmonitor.controller;

import com.example.floodmonitor.dto.AlertListResponse;
import com.example.floodmonitor.service.AlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // GET /api/v1/alerts?warningLevel=CRITICAL&river=Donau
    @GetMapping
    public AlertListResponse getAlerts(@RequestParam(required = false) String warningLevel,
                                       @RequestParam(required = false) String river) {
        return new AlertListResponse(alertService.getAlerts(QueryParams.parseWarningLevel(warningLevel), river));
    }
}