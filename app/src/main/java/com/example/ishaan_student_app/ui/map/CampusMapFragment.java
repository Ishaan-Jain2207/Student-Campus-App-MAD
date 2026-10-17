package com.example.ishaan_student_app.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ishaan_student_app.R;
import com.example.ishaan_student_app.data.model.CampusLocation;
import com.example.ishaan_student_app.data.repository.CampusLocationRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CampusMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "CampusMapFragment";
    private static final String MAP_FRAGMENT_TAG = "nmims_campus_google_map";

    private GoogleMap mMap;
    private Spinner spinnerCampusSelect;
    private TextView tvCampusName;
    private TextView tvCampusAddress;
    private Button btnDirections;

    private final List<CampusLocation> campusList = new ArrayList<>();
    private final Map<String, Marker> markerMap = new HashMap<>();
    private CampusLocation selectedCampus = null;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                Boolean fineLocation = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocation = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if ((fineLocation != null && fineLocation) || (coarseLocation != null && coarseLocation)) {
                    enableMyLocation();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_campus_map, container, false);

        spinnerCampusSelect = root.findViewById(R.id.spinner_campus_select);
        tvCampusName = root.findViewById(R.id.tv_selected_campus_name);
        tvCampusAddress = root.findViewById(R.id.tv_selected_campus_address);
        btnDirections = root.findViewById(R.id.btn_campus_directions);

        setupCampusSpinner();
        setupListeners();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!hasMapsApiKey()) {
            return;
        }

        TextView setupMessage = view.findViewById(R.id.tv_map_setup_message);
        if (setupMessage != null) {
            setupMessage.setVisibility(View.GONE);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentByTag(MAP_FRAGMENT_TAG);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.map_container, mapFragment, MAP_FRAGMENT_TAG)
                    .commit();
        }
        mapFragment.getMapAsync(this);
    }

    private void setupCampusSpinner() {
        campusList.clear();
        campusList.addAll(CampusLocationRepository.getCampusLocations());

        List<String> names = new ArrayList<>();
        for (CampusLocation loc : campusList) {
            names.add(loc.getName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, names);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCampusSelect.setAdapter(spinnerAdapter);

        if (!campusList.isEmpty()) {
            selectedCampus = campusList.get(0);
            updateSelectedCampusCard(selectedCampus);
        }

        spinnerCampusSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < campusList.size()) {
                    selectedCampus = campusList.get(position);
                    updateSelectedCampusCard(selectedCampus);
                    focusMapOnCampus(selectedCampus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupListeners() {
        btnDirections.setOnClickListener(v -> {
            if (selectedCampus != null) {
                openCampusDirections(selectedCampus);
            }
        });
    }

    private boolean hasMapsApiKey() {
        try {
            ApplicationInfo appInfo = requireContext().getPackageManager().getApplicationInfo(
                    requireContext().getPackageName(), PackageManager.GET_META_DATA);
            String apiKey = appInfo.metaData == null ? null
                    : appInfo.metaData.getString("com.google.android.geo.API_KEY");
            return apiKey != null && !apiKey.trim().isEmpty();
        } catch (PackageManager.NameNotFoundException missingPackage) {
            return false;
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        markerMap.clear();
        for (CampusLocation loc : campusList) {
            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(loc.getName())
                    .snippet(loc.getDescription()));

            if (marker != null) {
                marker.setTag(loc);
                markerMap.put(loc.getId(), marker);
            }
        }

        // On Red Pin Click -> Focus camera, show Info Window, update card
        mMap.setOnMarkerClickListener(marker -> {
            CampusLocation loc = (CampusLocation) marker.getTag();
            if (loc != null) {
                selectedCampus = loc;
                updateSelectedCampusCard(loc);
                syncSpinnerSelection(loc);
            }
            marker.showInfoWindow();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16.5f));
            return true;
        });

        // On Info Window Click -> Open Google Maps directions
        mMap.setOnInfoWindowClickListener(marker -> {
            CampusLocation loc = (CampusLocation) marker.getTag();
            if (loc != null) {
                openCampusDirections(loc);
            }
        });

        if (selectedCampus != null) {
            focusMapOnCampus(selectedCampus);
        }

        checkLocationPermissionsAndEnable();
    }

    private void focusMapOnCampus(CampusLocation campus) {
        if (mMap == null || campus == null) return;
        LatLng latLng = new LatLng(campus.getLatitude(), campus.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.5f));

        Marker marker = markerMap.get(campus.getId());
        if (marker != null) {
            marker.showInfoWindow();
        }
    }

    private void syncSpinnerSelection(CampusLocation campus) {
        for (int i = 0; i < campusList.size(); i++) {
            if (campusList.get(i).getId().equalsIgnoreCase(campus.getId())) {
                spinnerCampusSelect.setSelection(i);
                break;
            }
        }
    }

    private void updateSelectedCampusCard(CampusLocation campus) {
        if (campus == null) return;
        tvCampusName.setText(campus.getName());
        tvCampusAddress.setText(campus.getDescription());
    }

    private void openCampusDirections(CampusLocation campus) {
        if (campus == null) return;
        String uriStr = String.format(java.util.Locale.US, "geo:%f,%f?q=%f,%f(%s)",
                campus.getLatitude(), campus.getLongitude(),
                campus.getLatitude(), campus.getLongitude(),
                Uri.encode(campus.getName()));

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriStr));
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(mapIntent);
        } catch (Exception e) {
            // Fallback to web browser directions URL
            String webUrl = String.format(java.util.Locale.US,
                    "https://www.google.com/maps/search/?api=1&query=%f,%f",
                    campus.getLatitude(), campus.getLongitude());
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
            try {
                startActivity(webIntent);
            } catch (Exception ex) {
                Toast.makeText(requireContext(), "Could not open map link.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkLocationPermissionsAndEnable() {
        if (getContext() == null) return;
        boolean fineGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {
            enableMyLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void enableMyLocation() {
        if (mMap == null || getContext() == null) return;
        try {
            boolean fineGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean coarseGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (fineGranted || coarseGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
        } catch (SecurityException ignored) {
        }
    }
}
