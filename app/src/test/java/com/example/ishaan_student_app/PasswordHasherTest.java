package com.example.ishaan_student_app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ishaan_student_app.util.PasswordHasher;
import org.junit.Test;

public class PasswordHasherTest {

    @Test
    public void saltedHashMatchesOnlyTheOriginalPassword() throws Exception {
        byte[] salt = PasswordHasher.newSalt();
        byte[] hash = PasswordHasher.hash("CampusMatePass123", salt);

        assertTrue(PasswordHasher.matches("CampusMatePass123", salt, hash));
        assertFalse(PasswordHasher.matches("DifferentPass123", salt, hash));
    }

    @Test
    public void generatedSaltsDiffer() {
        byte[] first = PasswordHasher.newSalt();
        byte[] second = PasswordHasher.newSalt();

        assertFalse(java.util.Arrays.equals(first, second));
    }
}
