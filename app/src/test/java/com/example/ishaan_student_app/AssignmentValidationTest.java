package com.example.ishaan_student_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.ishaan_student_app.data.model.Assignment;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AssignmentValidationTest {

    @Test
    public void testAssignmentModelFieldsAndState() {
        Assignment assignment = new Assignment("Lab Report 2", "Data Structures", "28-10-2026", false);

        assertNotNull(assignment.getTitle());
        assertEquals("Lab Report 2", assignment.getTitle());
        assertEquals("Data Structures", assignment.getSubject());
        assertEquals("28-10-2026", assignment.getDueDate());
        assertFalse(assignment.isCompleted());

        assignment.setCompleted(true);
        assertTrue(assignment.isCompleted());
    }

    @Test
    public void testTemporalDueDateComparison() {
        String today = new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date());

        Assignment todayAssignment = new Assignment("Today Task", "Algorithms", today, false);
        assertEquals(0, todayAssignment.getDueDate().compareTo(today));
    }
}
