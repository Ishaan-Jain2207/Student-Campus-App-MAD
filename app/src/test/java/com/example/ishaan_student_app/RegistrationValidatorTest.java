package com.example.ishaan_student_app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ishaan_student_app.util.RegistrationValidator;

import org.junit.Test;

public class RegistrationValidatorTest {

    @Test
    public void testValidNmimsEmail() {
        assertTrue(RegistrationValidator.isValidNmimsEmail("student@nmims.in"));
        assertTrue(RegistrationValidator.isValidNmimsEmail("john.doe2026@nmims.in"));
    }

    @Test
    public void testInvalidNmimsEmail() {
        assertFalse(RegistrationValidator.isValidNmimsEmail(null));
        assertFalse(RegistrationValidator.isValidNmimsEmail(""));
        assertFalse(RegistrationValidator.isValidNmimsEmail("student@gmail.com"));
        assertFalse(RegistrationValidator.isValidNmimsEmail("student@nmims.edu"));
        assertFalse(RegistrationValidator.isValidNmimsEmail("student@nmims.in.fake.com"));
    }

    @Test
    public void testSapIdValidation() {
        assertTrue(RegistrationValidator.isValidSapId("700112233"));
        assertTrue(RegistrationValidator.isValidSapId("7001234567"));

        assertFalse(RegistrationValidator.isValidSapId(null));
        assertFalse(RegistrationValidator.isValidSapId("123")); // too short
        assertFalse(RegistrationValidator.isValidSapId("70011abc")); // non-numeric
    }

    @Test
    public void testYearValidation() {
        assertTrue(RegistrationValidator.isValidYear(1));
        assertTrue(RegistrationValidator.isValidYear(3));
        assertTrue(RegistrationValidator.isValidYear(5));

        assertFalse(RegistrationValidator.isValidYear(0));
        assertFalse(RegistrationValidator.isValidYear(6));
        assertFalse(RegistrationValidator.isValidYear(-1));
    }

    @Test
    public void testDepartmentValidation() {
        assertTrue(RegistrationValidator.isValidDepartment("STME"));
        assertTrue(RegistrationValidator.isValidDepartment("SOC"));
        assertTrue(RegistrationValidator.isValidDepartment("SOL"));

        assertFalse(RegistrationValidator.isValidDepartment(null));
        assertFalse(RegistrationValidator.isValidDepartment(""));
        assertFalse(RegistrationValidator.isValidDepartment("ENGINEERING"));
    }

    @Test
    public void testPasswordMatching() {
        assertTrue(RegistrationValidator.isValidPassword("secret123"));
        assertFalse(RegistrationValidator.isValidPassword("short"));

        assertTrue(RegistrationValidator.doPasswordsMatch("pass1234", "pass1234"));
        assertFalse(RegistrationValidator.doPasswordsMatch("pass1234", "pass5678"));
    }
}
