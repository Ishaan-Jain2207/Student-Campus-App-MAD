package com.example.ishaan_student_app.util;

import java.util.regex.Pattern;
import java.util.Locale;

/**
 * Validation utility for SVKM's NMIMS Chandigarh registration & authentication rules.
 */
public class RegistrationValidator {

    // Strict regex enforcing institutional @nmims.in email domain
    private static final Pattern NMIMS_EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@nmims\\.in$", Pattern.CASE_INSENSITIVE);

    public static boolean isValidFullName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    public static boolean isValidSapId(String sapId) {
        if (sapId == null || sapId.trim().isEmpty()) {
            return false;
        }
        String trimmed = sapId.trim();
        return trimmed.matches("^\\d{6,12}$");
    }

    public static boolean isValidNmimsEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return NMIMS_EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidYear(int year) {
        return year >= 1 && year <= 5;
    }

    public static boolean isValidDepartment(String department) {
        if (department == null) {
            return false;
        }
        String dept = department.trim().toUpperCase(Locale.ROOT);
        return "STME".equals(dept) || "SOC".equals(dept) || "SOL".equals(dept);
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.trim().length() >= 8;
    }

    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }
}
