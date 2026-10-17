package com.example.ishaan_student_app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ishaan_student_app.ui.main.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

@HiltAndroidTest
@RunWith(AndroidJUnit4.class)
public class MainNavigationTest {

    @Rule
    public final HiltAndroidRule hiltRule = new HiltAndroidRule(this);

    @Test
    public void attendanceIsAQuickDestinationAndBackReturnsHome() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        try {
            onView(withId(R.id.nav_attendance)).perform(click());
            onView(withText("Your attendance")).check(matches(isDisplayed()));

            pressBack();
            onView(withText("Daily Briefing")).check(matches(isDisplayed()));
        } finally {
            scenario.close();
        }
    }

    @Test
    public void assignmentsHasADirectPrimaryNavigationDestination() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        try {
            onView(withId(R.id.nav_assignments)).perform(click());
            onView(withId(R.id.btn_add_assignment_dialog)).check(matches(isDisplayed()));
        } finally {
            scenario.close();
        }
    }
}
