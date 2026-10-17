package com.example.ishaan_student_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ishaan_student_app.data.model.SubjectAttendanceSummary;

import org.junit.Test;

public class AttendanceThresholdTest {

    @Test
    public void testStmeBoundary79point99BelowThreshold() {
        // 7999 attended out of 10000 conducted = 79.99% < 80.0%
        SubjectAttendanceSummary sub = new SubjectAttendanceSummary("s1", "CS Test", "CS101", "STME", 1, 10000, 7999);
        assertEquals(79.99, sub.getAttendancePercentage(), 0.001);
        assertTrue(sub.isBelowDepartmentThreshold());
    }

    @Test
    public void testStmeBoundary80point00MeetsThreshold() {
        // 8000 attended out of 10000 conducted = 80.00% == 80.0%
        SubjectAttendanceSummary sub = new SubjectAttendanceSummary("s2", "CS Test", "CS101", "STME", 1, 10000, 8000);
        assertEquals(80.00, sub.getAttendancePercentage(), 0.001);
        assertFalse(sub.isBelowDepartmentThreshold());
    }

    @Test
    public void testStmeBoundary80point01MeetsThreshold() {
        // 8001 attended out of 10000 conducted = 80.01% > 80.0%
        SubjectAttendanceSummary sub = new SubjectAttendanceSummary("s3", "CS Test", "CS101", "STME", 1, 10000, 8001);
        assertEquals(80.01, sub.getAttendancePercentage(), 0.001);
        assertFalse(sub.isBelowDepartmentThreshold());
    }

    @Test
    public void testSolBoundary69point99BelowThreshold() {
        // 6999 attended out of 10000 conducted = 69.99% < 70.0%
        SubjectAttendanceSummary sub = new SubjectAttendanceSummary("s4", "Law Test", "LAW101", "SOL", 1, 10000, 6999);
        assertEquals(69.99, sub.getAttendancePercentage(), 0.001);
        assertTrue(sub.isBelowDepartmentThreshold());
    }

    @Test
    public void testSolBoundary70point00MeetsThreshold() {
        // 7000 attended out of 10000 conducted = 70.00% == 70.0%
        SubjectAttendanceSummary sub = new SubjectAttendanceSummary("s5", "Law Test", "LAW101", "SOL", 1, 10000, 7000);
        assertEquals(70.00, sub.getAttendancePercentage(), 0.001);
        assertFalse(sub.isBelowDepartmentThreshold());
    }

    @Test
    public void testSolBoundary70point01MeetsThreshold() {
        // 7001 attended out of 10000 conducted = 70.01% > 70.0%
        SubjectAttendanceSummary sub = new SubjectAttendanceSummary("s6", "Law Test", "LAW101", "SOL", 1, 10000, 7001);
        assertEquals(70.01, sub.getAttendancePercentage(), 0.001);
        assertFalse(sub.isBelowDepartmentThreshold());
    }

    @Test
    public void testZeroConductedSessionsNoWarning() {
        SubjectAttendanceSummary sub = new SubjectAttendanceSummary("s7", "Empty Course", "CS000", "STME", 1, 0, 0);
        assertEquals(0.0, sub.getAttendancePercentage(), 0.001);
        assertFalse(sub.isBelowDepartmentThreshold());
    }
}
