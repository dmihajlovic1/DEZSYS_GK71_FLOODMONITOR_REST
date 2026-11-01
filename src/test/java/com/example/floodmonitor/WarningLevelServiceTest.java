package com.example.floodmonitor;

import com.example.floodmonitor.model.*;
import com.example.floodmonitor.service.WarningLevelService;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WarningLevelServiceTest {

    private final WarningLevelService service = new WarningLevelService();

    // Normal 250, Warnung ab 350, kritisch ab 450
    private final Station station = new Station("ST-TEST", "Test", "Donau",
            new Location(48.0, 14.0), 250, 350, 450, true, StationStatus.ONLINE);

    private Measurement withLevel(double level) {
        return new Measurement("ST-TEST", Instant.now(), level, 500, 0, 15, 90, StationStatus.ONLINE, null);
    }

    @Test
    void normalUnterWarnwert() {
        assertEquals(WarningLevel.NORMAL, service.calculate(station, withLevel(349.9)));
    }

    @Test
    void warningAbWarnwert() {
        assertEquals(WarningLevel.WARNING, service.calculate(station, withLevel(350)));
    }

    @Test
    void criticalAbKritischemWert() {
        assertEquals(WarningLevel.CRITICAL, service.calculate(station, withLevel(450)));
    }

    @Test
    void unknownOhneMessung() {
        assertEquals(WarningLevel.UNKNOWN, service.calculate(station, null));
    }
}