package com.example.ishaan_student_app.data.repository;

import com.example.ishaan_student_app.data.model.CampusLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Single maintainable repository defining SVKM's NMIMS Chandigarh campus address location and coordinates.
 * Campus Address: Plot No. 5, Education City, Opposite Botanical Gardens, Sarangpur, Chandigarh 160014.
 * Coordinates are taken from the Google Maps embed on NMIMS Chandigarh's official contact page.
 */
public class CampusLocationRepository {

    public static final double NMIMS_CHANDIGARH_LAT = 30.7832533;
    public static final double NMIMS_CHANDIGARH_LNG = 76.7619824;

    public static List<CampusLocation> getCampusLocations() {
        List<CampusLocation> locations = new ArrayList<>();

        locations.add(new CampusLocation(
                "loc_nmims_main",
                "SVKM's NMIMS Chandigarh Main Campus",
                "Plot No. 5, Education City, Opposite Botanical Gardens, Adjacent to Govt Senior Secondary School, Sarangpur, Chandigarh 160014",
                NMIMS_CHANDIGARH_LAT,
                NMIMS_CHANDIGARH_LNG
        ));

        return locations;
    }
}
