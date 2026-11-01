package com.example.floodmonitor.model;

public class Location {
    private double latitude;   // Breitengrad
    private double longitude;  // Längengrad

    public Location() {}       // leerer Konstruktor -> braucht Jackson

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}