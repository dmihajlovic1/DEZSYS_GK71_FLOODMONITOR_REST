package com.example.floodmonitor;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.StationStatus;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeasurementRepository measurementRepository;

    /** Vor jedem Test: feste, bekannte Testdaten (keine Zufallswerte). */
    @BeforeEach
    void setUp() {
        measurementRepository.clear();
        add("2026-05-01T08:00:00Z", 300);
        add("2026-05-01T10:00:00Z", 360);
        add("2026-05-01T12:00:00Z", 460);
    }

    private void add(String time, double level) {
        measurementRepository.add(new Measurement("ST-001", Instant.parse(time), level, 700, 5, 14, 90,
                StationStatus.ONLINE, WarningLevel.NORMAL));
    }

    // 1. Alle Stationen als JSON
    @Test
    void alleStationenAlsJson() throws Exception {
        mockMvc.perform(get("/api/v1/stations").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(5))
                .andExpect(jsonPath("$.stations[0].id").value("ST-001"));
    }

    // 2. Alle Stationen als XML
    @Test
    void alleStationenAlsXml() throws Exception {
        mockMvc.perform(get("/api/v1/stations").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(xpath("/stations/count").string("5"))
                .andExpect(xpath("/stations/station[1]/id").string("ST-001"));
    }

    // 3. Existierende Station
    @Test
    void existierendeStation() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ST-002"))
                .andExpect(jsonPath("$.river").value("Donau"));
    }

    // 4. Unbekannte Station -> 404
    @Test
    void unbekannteStation() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Station ST-999 wurde nicht gefunden"));
    }

    // 5. Aktuellster Messwert
    @Test
    void aktuellsterMesswert() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").value("2026-05-01T12:00:00Z"))
                .andExpect(jsonPath("$.waterLevel").value(460.0));
    }

    // 6. Filterung nach Zeitraum
    @Test
    void filterNachZeitraum() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("from", "2026-05-01T09:00:00Z")
                        .param("to", "2026-05-01T11:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.measurements[0].timestamp").value("2026-05-01T10:00:00Z"));
    }

    // 7. Ungültiger Zeitraum (from nach to) -> 400
    @Test
    void ungueltigerZeitraum() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("from", "2026-05-01T12:00:00Z")
                        .param("to", "2026-05-01T08:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // 11. Nicht unterstütztes Format -> 406
    @Test
    void nichtUnterstuetztesFormat() throws Exception {
        mockMvc.perform(get("/api/v1/stations").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isNotAcceptable());
    }

    // 12. Maximales Limit: 1000 erlaubt, 1001 nicht
    @Test
    void maximalesLimitErlaubt() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements").param("limit", "1000"))
                .andExpect(status().isOk());
    }

    @Test
    void limitUeberMaximum() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements").param("limit", "1001"))
                .andExpect(status().isBadRequest());
    }
}