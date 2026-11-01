package com.example.floodmonitor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "alerts")
public class AlertListResponse {

    private final List<Alert> alerts;

    public AlertListResponse(List<Alert> alerts) {
        this.alerts = alerts;
    }

    public int getCount() {
        return alerts.size();
    }

    @JsonProperty("alerts")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "alert")
    public List<Alert> getAlerts() {
        return alerts;
    }
}