package com.example.ishaan_student_app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.ishaan_student_app.data.db.DatabaseHelper;
import com.example.ishaan_student_app.data.model.Student;
import com.example.ishaan_student_app.util.PasswordHasher;
import com.example.ishaan_student_app.util.RegistrationValidator;

import dagger.hilt.android.qualifiers.ApplicationContext;
import java.security.GeneralSecurityException;
import java.util.Locale;
import javax.inject.Inject;

/** Creates and authenticates explicitly device-local CampusMate accounts. */
public class LocalAccountRepository {

    private static final String TAG = "LocalAccountRepo";

    public enum RegistrationResult {
        CREATED,
        SAP_ID_ALREADY_USED,
        EMAIL_ALREADY_USED,
        INVALID_INPUT,
        STORAGE_ERROR
    }

    private final DatabaseHelper dbHelper;

    @Inject
    public LocalAccountRepository(@ApplicationContext Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public RegistrationResult register(Student student, String password) {
        if (!isValidStudent(student) || !RegistrationValidator.isValidPassword(password)) {
            return RegistrationResult.INVALID_INPUT;
        }

        String sapId = student.getSapId().trim();
        String email = normalizeEmail(student.getEmail());
        byte[] salt;
        byte[] passwordHash;
        try {
            salt = PasswordHasher.newSalt();
            passwordHash = PasswordHasher.hash(password, salt);
        } catch (GeneralSecurityException | RuntimeException e) {
            Log.e(TAG, "Error hashing password for sapId: " + sapId, e);
            return RegistrationResult.STORAGE_ERROR;
        }

        SQLiteDatabase db;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
        } catch (Exception e) {
            Log.e(TAG, "Database access error", e);
            return RegistrationResult.STORAGE_ERROR;
        }

        RegistrationResult result = RegistrationResult.STORAGE_ERROR;
        try {
            if (exists(db, DatabaseHelper.TABLE_STUDENTS, DatabaseHelper.COL_STUDENT_SAP_ID, sapId)
                    || exists(db, DatabaseHelper.TABLE_LOCAL_ACCOUNTS, DatabaseHelper.COL_STUDENT_SAP_ID, sapId)) {
                result = RegistrationResult.SAP_ID_ALREADY_USED;
            } else if (emailExists(db, DatabaseHelper.TABLE_STUDENTS, email)
                    || emailExists(db, DatabaseHelper.TABLE_LOCAL_ACCOUNTS, email)) {
                result = RegistrationResult.EMAIL_ALREADY_USED;
            } else {
                ContentValues studentValues = new ContentValues();
                studentValues.put(DatabaseHelper.COL_STUDENT_SAP_ID, sapId);
                studentValues.put(DatabaseHelper.COL_STUDENT_NAME, student.getName().trim());
                studentValues.put(DatabaseHelper.COL_STUDENT_EMAIL, email);
                studentValues.put(DatabaseHelper.COL_STUDENT_DEPT, student.getDepartment().trim().toUpperCase(Locale.ROOT));
                studentValues.put(DatabaseHelper.COL_STUDENT_YEAR, student.getYear());
                studentValues.put(DatabaseHelper.COL_STUDENT_PHOTO_URI, "");
                db.insertOrThrow(DatabaseHelper.TABLE_STUDENTS, null, studentValues);

                ContentValues accountValues = new ContentValues();
                accountValues.put(DatabaseHelper.COL_STUDENT_SAP_ID, sapId);
                accountValues.put(DatabaseHelper.COL_STUDENT_EMAIL, email);
                accountValues.put(DatabaseHelper.COL_AUTH_SALT, salt);
                accountValues.put(DatabaseHelper.COL_AUTH_HASH, passwordHash);
                accountValues.put("created_at", System.currentTimeMillis());
                db.insertOrThrow(DatabaseHelper.TABLE_LOCAL_ACCOUNTS, null, accountValues);

                db.setTransactionSuccessful();
                result = RegistrationResult.CREATED;
            }
        } catch (SQLiteConstraintException e) {
            Log.e(TAG, "Constraint exception during registration for sapId: " + sapId, e);
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase(Locale.ROOT) : "";
            if (msg.contains("email")) {
                result = RegistrationResult.EMAIL_ALREADY_USED;
            } else if (msg.contains("sap_id") || msg.contains("students") || msg.contains("local_accounts")) {
                result = RegistrationResult.SAP_ID_ALREADY_USED;
            } else {
                result = RegistrationResult.STORAGE_ERROR;
            }
        } catch (Exception e) {
            Log.e(TAG, "Storage exception during registration for sapId: " + sapId, e);
            result = RegistrationResult.STORAGE_ERROR;
        } finally {
            try {
                if (db.inTransaction()) {
                    db.endTransaction();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error ending transaction", e);
            }
        }
        return result;
    }

    public boolean authenticate(String sapId, String email, String password) {
        if (!RegistrationValidator.isValidSapId(sapId)
                || !RegistrationValidator.isValidNmimsEmail(email)
                || password == null || password.isEmpty()) {
            return false;
        }

        byte[] salt;
        byte[] expectedHash;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            try (Cursor cursor = db.rawQuery(
                    "SELECT " + DatabaseHelper.COL_AUTH_SALT + ", " + DatabaseHelper.COL_AUTH_HASH
                            + " FROM " + DatabaseHelper.TABLE_LOCAL_ACCOUNTS
                            + " WHERE " + DatabaseHelper.COL_STUDENT_SAP_ID + " = ? AND "
                            + DatabaseHelper.COL_STUDENT_EMAIL + " = ? COLLATE NOCASE LIMIT 1",
                    new String[]{sapId.trim(), normalizeEmail(email)})) {
                if (!cursor.moveToFirst()) {
                    return false;
                }
                salt = cursor.getBlob(0);
                expectedHash = cursor.getBlob(1);
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error during authentication query", e);
            return false;
        }
        return PasswordHasher.matches(password, salt, expectedHash);
    }

    private boolean isValidStudent(Student student) {
        return student != null
                && RegistrationValidator.isValidSapId(student.getSapId())
                && RegistrationValidator.isValidFullName(student.getName())
                && RegistrationValidator.isValidNmimsEmail(student.getEmail())
                && RegistrationValidator.isValidDepartment(student.getDepartment())
                && RegistrationValidator.isValidYear(student.getYear());
    }

    private boolean exists(SQLiteDatabase db, String table, String column, String value) {
        try (Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + table + " WHERE " + column + " = ? LIMIT 1",
                new String[]{value})) {
            return cursor.moveToFirst();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean emailExists(SQLiteDatabase db, String table, String email) {
        try (Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + table + " WHERE " + DatabaseHelper.COL_STUDENT_EMAIL
                        + " = ? COLLATE NOCASE LIMIT 1",
                new String[]{email})) {
            return cursor.moveToFirst();
        } catch (Exception e) {
            return false;
        }
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
