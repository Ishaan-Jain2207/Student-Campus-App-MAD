package com.example.ishaan_student_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ishaan_student_app.data.model.Assignment;
import com.example.ishaan_student_app.data.model.Student;

import org.junit.Test;

public class RepositoryContractTest {

    @Test
    public void testStudentModelContracts() {
        Student student = new Student(1, "700112233", "Ishaan Jain", "ishaan.jain@nmims.in", "STME", 3, "");
        assertEquals(1, student.getId());
        assertEquals("700112233", student.getSapId());
        assertEquals("Ishaan Jain", student.getName());
        assertEquals("ishaan.jain@nmims.in", student.getEmail());
        assertEquals("STME", student.getDepartment());
        assertEquals(3, student.getYear());
    }

    @Test
    public void testAssignmentModelContracts() {
        Assignment assignment = new Assignment("Algorithms Assignment", "Algorithms", "2026-10-25", false);
        assertEquals("Algorithms Assignment", assignment.getTitle());
        assertEquals("Algorithms", assignment.getSubject());
        assertEquals("2026-10-25", assignment.getDueDate());
        assertFalse(assignment.isCompleted());

        assignment.setCompleted(true);
        assertTrue(assignment.isCompleted());
    }
}
