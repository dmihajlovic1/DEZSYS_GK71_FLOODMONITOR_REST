package com.example.floodmonitor.controller;

import com.example.floodmonitor.exception.BadRequestException;
import com.example.floodmonitor.model.WarningLevel;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

/** Liest und validiert Query-Parameter. Ungültige Werte -> 400 Bad Request. */
public final class QueryParams {

    public static final int DEFAULT_LIMIT = 100;
    public static final int MAX_LIMIT = 1000;

    private QueryParams() {}

    public static Instant parseInstant(String name, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value.trim()).toInstant();
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Ungültiges Datumsformat für '" + name + "': '" + value
                    + "'. Erwartet ISO-8601, z. B. 2026-05-01T08:00:00Z");
        }
    }

    public static void checkRange(Instant from, Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("Ungültiger Zeitraum: 'from' (" + from + ") liegt nach 'to' (" + to + ")");
        }
    }

    public static WarningLevel parseWarningLevel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return WarningLevel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Ungültige Warnstufe '" + value + "'. Erlaubt: "
                    + Arrays.toString(WarningLevel.values()));
        }
    }

    public static int parseLimit(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_LIMIT;
        }
        int limit;
        try {
            limit = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("Ungültiger Wert für 'limit': '" + value + "'. Erwartet eine Zahl");
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BadRequestException("'limit' muss zwischen 1 und " + MAX_LIMIT + " liegen, war " + limit);
        }
        return limit;
    }

    public static Boolean parseBoolean(String name, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.equalsIgnoreCase("true")) return true;
        if (value.equalsIgnoreCase("false")) return false;
        throw new BadRequestException("Ungültiger Wert für '" + name + "': '" + value + "'. Erlaubt: true, false");
    }
}