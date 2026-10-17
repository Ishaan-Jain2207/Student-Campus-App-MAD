package com.example.ishaan_student_app.data.repository;

import com.example.ishaan_student_app.data.model.CampusLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository defining SVKM's NMIMS campuses across India with Google Maps coordinates.
 */
public class CampusLocationRepository {

    public static final double NMIMS_CHANDIGARH_LAT = 30.78346392270489;
    public static final double NMIMS_CHANDIGARH_LNG = 76.76151082103206;

    public static List<CampusLocation> getCampusLocations() {
        List<CampusLocation> locations = new ArrayList<>();

        locations.add(new CampusLocation(
                "loc_nmims_chandigarh",
                "NMIMS Chandigarh",
                "Plot No. 5, Education City, Sarangpur, Chandigarh",
                30.78346392270489,
                76.76151082103206
        ));

        locations.add(new CampusLocation(
                "loc_nmims_mumbai",
                "NMIMS Mumbai (Main Campus)",
                "V. L. Mehta Road, Vile Parle West, Mumbai, Maharashtra",
                19.10359768382598,
                72.83645932062434
        ));

        locations.add(new CampusLocation(
                "loc_nmims_navi_mumbai",
                "NMIMS Navi Mumbai",
                "Plot No. 2, Sector 33, Kharghar, Navi Mumbai, Maharashtra",
                19.064766787587654,
                73.07780236850668
        ));

        locations.add(new CampusLocation(
                "loc_nmims_bengaluru",
                "NMIMS Bengaluru",
                "Bannerghatta Main Road, Kalkere Post, Anekal Taluk, Bengaluru, Karnataka",
                12.82524491094464,
                77.58804240884093
        ));

        locations.add(new CampusLocation(
                "loc_nmims_hyderabad",
                "NMIMS Hyderabad",
                "Plot No. B4, Green Industrial Park, Polepally SEZ, Jadcherla, Telangana",
                16.825070083969358,
                78.14244976659774
        ));

        locations.add(new CampusLocation(
                "loc_nmims_indore",
                "NMIMS Indore",
                "Super Corridor Road, Gandhi Nagar, Indore, Madhya Pradesh",
                22.74955112697323,
                75.79446606491221
        ));

        locations.add(new CampusLocation(
                "loc_nmims_shirpur",
                "NMIMS Shirpur",
                "Mukesh Patel Technology Park, Babulde, Bank of Tapi River, NH-3, Shirpur, Maharashtra",
                21.28574551441558,
                74.84196315943551
        ));

        locations.add(new CampusLocation(
                "loc_nmims_dhule",
                "NMIMS Dhule",
                "Survey No. 499, Plot No. 2, Behind Gurudwara, Samta Nagar, Dhule, Maharashtra",
                20.870195828521904,
                74.7681692317849
        ));

        return locations;
    }
}
