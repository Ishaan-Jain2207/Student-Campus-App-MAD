package com.example.ishaan_student_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ishaan_student_app.data.model.SubjectAttendanceSummary;

import org.junit.Test;

public class SubjectAttendanceSummaryTest {

    @Test
    public void testAttendancePercentageCalculation() {
        SubjectAttendanceSummary enrollment = new SubjectAttendanceSummary(
                "sub_1", "Data Structures", "CS301", "STME", 3, 20, 18
        );
        assertEquals(90.0, enrollment.getAttendancePercentage(), 0.001);
        assertFalse(enrollment.isBelowDepartmentThreshold());
    }

    @Test
    public void testStmeThresholdWarning() {
        // STME threshold is 80%
        // 13/18 = 72.22% -> Below 80% threshold!
        SubjectAttendanceSummary enrollment = new SubjectAttendanceSummary(
                "sub_2", "Software Engineering", "CS303", "STME", 3, 18, 13
        );
        assertEquals(72.222, enrollment.getAttendancePercentage(), 0.01);
        assertTrue(enrollment.isBelowDepartmentThreshold());
    }

    @Test
    public void testSocThresholdWarning() {
        // SOC threshold is 80%
        // 15/20 = 75% -> Below 80% threshold!
        SubjectAttendanceSummary enrollment = new SubjectAttendanceSummary(
                "sub_3", "Financial Accounting", "COMM201", "SOC", 2, 20, 15
        );
        assertEquals(75.0, enrollment.getAttendancePercentage(), 0.001);
        assertTrue(enrollment.isBelowDepartmentThreshold());
    }

    @Test
    public void testSolThresholdWarning() {
        // SOL threshold is 70%
        // 15/20 = 75% -> Above SOL 70% threshold!
        SubjectAttendanceSummary enrollmentSol = new SubjectAttendanceSummary(
                "sub_4", "Constitutional Law", "LAW101", "SOL", 1, 20, 15
        );
        assertEquals(75.0, enrollmentSol.getAttendancePercentage(), 0.001);
        assertFalse(enrollmentSol.isBelowDepartmentThreshold());

        // 12/20 = 60% -> Below SOL 70% threshold!
        SubjectAttendanceSummary enrollmentSolLow = new SubjectAttendanceSummary(
                "sub_5", "Criminal Law", "LAW102", "SOL", 1, 20, 12
        );
        assertEquals(60.0, enrollmentSolLow.getAttendancePercentage(), 0.001);
        assertTrue(enrollmentSolLow.isBelowDepartmentThreshold());
    }

    @Test
    public void testZeroConductedSessions() {
        SubjectAttendanceSummary enrollment = new SubjectAttendanceSummary(
                "sub_6", "New Course", "CS305", "STME", 3, 0, 0
        );
        assertEquals(0.0, enrollment.getAttendancePercentage(), 0.001);
        assertFalse(enrollment.isBelowDepartmentThreshold());
    }

    @Test
    public void unknownDepartmentDoesNotAssumeAnAttendanceTarget() {
        SubjectAttendanceSummary summary = new SubjectAttendanceSummary(
                "sub_7", "New Course", "", "", 0, 10, 2
        );
        assertEquals(0.0, summary.getDepartmentThreshold(), 0.001);
        assertFalse(summary.isBelowDepartmentThreshold());
    }

    @Test
    public void invalidSessionCountsAreClamped() {
        SubjectAttendanceSummary summary = new SubjectAttendanceSummary(
                "sub_8", "New Course", "", "STME", 1, 2, 9
        );
        assertEquals(2, summary.getTotalConductedSessions());
        assertEquals(2, summary.getTotalAttendedSessions());
        assertEquals(100.0, summary.getAttendancePercentage(), 0.001);
    }
}
