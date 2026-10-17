package com.example.ishaan_student_app.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.ishaan_student_app.R;
import com.example.ishaan_student_app.data.session.SessionManager;
import com.example.ishaan_student_app.ui.assignments.AssignmentsFragment;
import com.example.ishaan_student_app.ui.attendance.AttendanceFragment;
import com.example.ishaan_student_app.ui.home.HomeFragment;
import com.example.ishaan_student_app.ui.events.EventsFragment;
import com.example.ishaan_student_app.ui.login.LoginActivity;
import com.example.ishaan_student_app.ui.map.CampusMapFragment;
import com.example.ishaan_student_app.ui.profile.ProfileFragment;
import com.example.ishaan_student_app.ui.resources.ResourcesFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

/**
 * Main application shell hosting navigation drawer, bottom navigation bar,
 * and Fragment destinations.
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;

    @Inject SessionManager sessionManager;
    private int currentDestinationId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupNavigation();
        setupBackNavigation();

        if (savedInstanceState == null) {
            navigateToDestination(R.id.nav_home, new HomeFragment(), getString(R.string.title_home));
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navigationView = findViewById(R.id.navigation_drawer);
    }

    private void setupNavigation() {
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == currentDestinationId) {
                return true;
            }

            if (itemId == R.id.nav_home) {
                return navigateToDestination(R.id.nav_home, new HomeFragment(), getString(R.string.title_home));
            } else if (itemId == R.id.nav_attendance) {
                return navigateToDestination(R.id.nav_attendance, new AttendanceFragment(), getString(R.string.title_attendance));
            } else if (itemId == R.id.nav_assignments) {
                return navigateToDestination(R.id.nav_assignments, new AssignmentsFragment(), getString(R.string.title_assignments));
            } else if (itemId == R.id.nav_events) {
                return navigateToDestination(R.id.nav_events, new EventsFragment(), getString(R.string.title_events));
            } else if (itemId == R.id.nav_profile) {
                return navigateToDestination(R.id.nav_profile, new ProfileFragment(), getString(R.string.title_profile));
            }
            return false;
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int itemId = item.getItemId();

            if (itemId == R.id.drawer_map) {
                syncBottomNavSelection(-1);
                return navigateToDestination(R.id.drawer_map, new CampusMapFragment(), getString(R.string.title_map));
            } else if (itemId == R.id.drawer_resources) {
                syncBottomNavSelection(-1);
                return navigateToDestination(R.id.drawer_resources, new ResourcesFragment(), getString(R.string.title_resources));
            } else if (itemId == R.id.drawer_logout) {
                showLogoutConfirmation();
                return true;
            }
            return false;
        });
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (currentDestinationId != R.id.nav_home) {
                    syncBottomNavSelection(R.id.nav_home);
                    navigateToDestination(R.id.nav_home, new HomeFragment(), getString(R.string.title_home));
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    public void navigateToTab(int tabId) {
        if (bottomNavigationView.getMenu().findItem(tabId) != null) {
            bottomNavigationView.setSelectedItemId(tabId);
        }
    }

    public void openEvents() {
        navigateToTab(R.id.nav_events);
    }

    private boolean navigateToDestination(int destinationId, Fragment fragment, String title) {
        if (destinationId == currentDestinationId) {
            return true;
        }

        currentDestinationId = destinationId;
        toolbar.setTitle(title);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        return true;
    }

    private void syncBottomNavSelection(int navId) {
        if (navId != -1) {
            bottomNavigationView.getMenu().findItem(navId).setChecked(true);
        } else {
            bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
            for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                bottomNavigationView.getMenu().getItem(i).setChecked(false);
            }
            bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
        }
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.action_logout, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void performLogout() {
        sessionManager.logout();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
