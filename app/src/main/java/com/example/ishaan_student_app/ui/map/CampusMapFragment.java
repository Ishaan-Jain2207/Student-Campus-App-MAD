package com.example.ishaan_student_app.ui.map;

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ishaan_student_app.R;
import com.example.ishaan_student_app.data.repository.CampusLocationRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class CampusMapFragment extends Fragment implements OnMapReadyCallback {
    private static final String MAP_FRAGMENT_TAG = "nmims_campus_google_map";
    private GoogleMap mMap;

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
        return inflater.inflate(R.layout.fragment_campus_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!hasMapsApiKey()) {
            return;
        }

        TextView setupMessage = view.findViewById(R.id.tv_map_setup_message);
        setupMessage.setVisibility(View.GONE);

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
        LatLng campus = new LatLng(CampusLocationRepository.NMIMS_CHANDIGARH_LAT,
                CampusLocationRepository.NMIMS_CHANDIGARH_LNG);
        googleMap.addMarker(new MarkerOptions()
                .position(campus)
                .title(getString(R.string.campus_pin_title)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campus, 17.5f));
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        checkLocationPermissionsAndEnable();
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
