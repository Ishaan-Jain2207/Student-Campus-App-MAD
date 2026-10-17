package com.example.ishaan_student_app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ishaan_student_app.data.db.DatabaseHelper;
import com.example.ishaan_student_app.data.model.Student;
import com.example.ishaan_student_app.util.RegistrationValidator;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import java.util.Locale;

/**
 * Repository performing SQLite persistence operations for authenticated NMIMS Student profiles.
 */
public class StudentRepository {

    private final DatabaseHelper dbHelper;

    @Inject
    public StudentRepository(@ApplicationContext Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public Student getStudentBySapIdOrEmail(String sapId, String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Student student = null;

        if (sapId != null && !sapId.trim().isEmpty()) {
            student = queryStudent(db, DatabaseHelper.COL_STUDENT_SAP_ID, sapId.trim());
        }

        if (student == null && email != null && !email.trim().isEmpty()) {
            student = queryStudent(db, DatabaseHelper.COL_STUDENT_EMAIL, email.trim().toLowerCase(Locale.ROOT));
        }

        return student;
    }

    public Student getStudentBySapId(String sapId) {
        return getStudentBySapIdOrEmail(sapId, null);
    }

    private Student queryStudent(SQLiteDatabase db, String column, String value) {
        Student student = null;
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_STUDENTS + " WHERE " + column + " = ? LIMIT 1";
        try (Cursor cursor = db.rawQuery(query, new String[]{value})) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STUDENT_ID));
                String foundSapId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STUDENT_SAP_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STUDENT_NAME));
                String foundEmail = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STUDENT_EMAIL));
                String dept = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STUDENT_DEPT));
                int year = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STUDENT_YEAR));
                String photo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STUDENT_PHOTO_URI));

                student = new Student(id, foundSapId, name, foundEmail, dept, year, photo);
            }
        } catch (Exception ignored) {
        }
        return student;
    }

    public boolean updateStudent(Student student) {
        if (student == null || !RegistrationValidator.isValidFullName(student.getName())) {
            return false;
        }

        String sapId = student.getSapId() != null ? student.getSapId().trim() : "";
        String email = student.getEmail() != null ? student.getEmail().trim().toLowerCase(Locale.ROOT) : "";

        if (sapId.isEmpty() && email.isEmpty()) {
            return false;
        }

        String dept = RegistrationValidator.isValidDepartment(student.getDepartment())
                ? student.getDepartment().trim().toUpperCase(Locale.ROOT)
                : "STME";
        int year = RegistrationValidator.isValidYear(student.getYear()) ? student.getYear() : 1;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_STUDENT_NAME, student.getName().trim());
        if (!sapId.isEmpty()) {
            values.put(DatabaseHelper.COL_STUDENT_SAP_ID, sapId);
        }
        if (!email.isEmpty()) {
            values.put(DatabaseHelper.COL_STUDENT_EMAIL, email);
        }
        values.put(DatabaseHelper.COL_STUDENT_DEPT, dept);
        values.put(DatabaseHelper.COL_STUDENT_YEAR, year);
        values.put(DatabaseHelper.COL_STUDENT_PHOTO_URI, student.getPhotoUri() != null ? student.getPhotoUri() : "");

        int rows = 0;
        if (!sapId.isEmpty()) {
            rows = db.update(
                    DatabaseHelper.TABLE_STUDENTS,
                    values,
                    DatabaseHelper.COL_STUDENT_SAP_ID + " = ?",
                    new String[]{sapId}
            );
        }

        if (rows == 0 && !email.isEmpty()) {
            rows = db.update(
                    DatabaseHelper.TABLE_STUDENTS,
                    values,
                    DatabaseHelper.COL_STUDENT_EMAIL + " = ? COLLATE NOCASE",
                    new String[]{email}
            );
        }

        if (rows == 0) {
            long inserted = db.insertWithOnConflict(
                    DatabaseHelper.TABLE_STUDENTS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
            );
            return inserted != -1;
        }

        return true;
    }
}
