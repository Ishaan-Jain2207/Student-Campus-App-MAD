package com.example.ishaan_student_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.example.ishaan_student_app.data.model.CampusLocation;
import com.example.ishaan_student_app.data.repository.CampusLocationRepository;

import org.junit.Test;

import java.util.List;

public class CampusLocationTest {

    @Test
    public void testCampusLocationsCountAndData() {
        List<CampusLocation> locations = CampusLocationRepository.getCampusLocations();
        assertNotNull(locations);
        assertEquals(8, locations.size());

        CampusLocation chandigarh = locations.get(0);
        assertEquals("loc_nmims_chandigarh", chandigarh.getId());
        assertEquals("NMIMS Chandigarh", chandigarh.getName());
        assertEquals(30.78346392270489, chandigarh.getLatitude(), 0.0001);
        assertEquals(76.76151082103206, chandigarh.getLongitude(), 0.0001);
    }
}
