package com.example.ishaan_student_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.ishaan_student_app.data.model.CampusLocation;
import com.example.ishaan_student_app.data.repository.CampusLocationRepository;

import org.junit.Test;

import java.util.List;

public class CampusLocationTest {

    @Test
    public void testCampusLocationsCountAndData() {
        List<CampusLocation> locations = CampusLocationRepository.getCampusLocations();
        assertNotNull(locations);
        assertEquals(1, locations.size());

        CampusLocation mainLocation = locations.get(0);
        assertEquals("loc_nmims_main", mainLocation.getId());
        assertEquals("SVKM's NMIMS Chandigarh Main Campus", mainLocation.getName());
        assertEquals(CampusLocationRepository.NMIMS_CHANDIGARH_LAT, mainLocation.getLatitude(), 0.0001);
        assertEquals(CampusLocationRepository.NMIMS_CHANDIGARH_LNG, mainLocation.getLongitude(), 0.0001);
    }
}
