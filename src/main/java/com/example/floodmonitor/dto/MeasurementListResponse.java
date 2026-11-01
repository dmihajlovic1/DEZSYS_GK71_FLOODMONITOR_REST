package com.example.floodmonitor.dto;

import com.example.floodmonitor.model.Measurement;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "measurements")
public class MeasurementListResponse {

    private final String stationId;
    private final List<Measurement> measurements;

    public MeasurementListResponse(String stationId, List<Measurement> measurements) {
        this.stationId = stationId;
        this.measurements = measurements;
    }

    public String getStationId() {
        return stationId;
    }

    public int getCount() {
        return measurements.size();
    }

    @JsonProperty("measurements")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "measurement")
    public List<Measurement> getMeasurements() {
        return measurements;
    }
}