package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Location;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.StationStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class StationRepository {

    private final Map<String, Station> stations = new ConcurrentHashMap<>();

    public StationRepository() {
        // Startdaten: 5 Stationen, ST-005 ist ausgefallen (OFFLINE)
        save(new Station("ST-001", "Donaubruecke Linz", "Donau", new Location(48.3069, 14.2858), 250, 350, 450, true, StationStatus.ONLINE));
        save(new Station("ST-002", "Reichsbruecke Wien", "Donau", new Location(48.2290, 16.4087), 300, 420, 520, true, StationStatus.ONLINE));
        save(new Station("ST-003", "Innbruecke Innsbruck", "Inn", new Location(47.2692, 11.3933), 180, 260, 330, true, StationStatus.ONLINE));
        save(new Station("ST-004", "Staatsbruecke Salzburg", "Salzach", new Location(47.8013, 13.0440), 200, 320, 420, true, StationStatus.ONLINE));
        save(new Station("ST-005", "Hauptbruecke Graz", "Mur", new Location(47.0717, 15.4339), 150, 250, 330, true, StationStatus.OFFLINE));
    }

    public List<Station> findAll() {
        List<Station> list = new ArrayList<>(stations.values());
        list.sort(Comparator.comparing(Station::getId));
        return list;
    }

    public Optional<Station> findById(String id) {
        return Optional.ofNullable(stations.get(id));
    }

    public Station save(Station station) {
        stations.put(station.getId(), station);
        return station;
    }
}