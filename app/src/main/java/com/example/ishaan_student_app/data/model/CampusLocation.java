package com.example.ishaan_student_app.data.model;

/**
 * Model class representing a Campus Landmark/Location entry on Google Maps.
 */
public class CampusLocation {
    private final String id;
    private final String name;
    private final String description;
    private final double latitude;
    private final double longitude;

    public CampusLocation(String id, String name, String description, double latitude, double longitude) {
        this.id = id != null ? id.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
