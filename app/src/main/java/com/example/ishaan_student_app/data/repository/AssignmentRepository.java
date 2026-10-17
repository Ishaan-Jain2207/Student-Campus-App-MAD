package com.example.ishaan_student_app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ishaan_student_app.data.db.DatabaseHelper;
import com.example.ishaan_student_app.data.model.Assignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository performing account-scoped SQLite persistence operations for Assignments.
 */
public class AssignmentRepository {

    private final DatabaseHelper dbHelper;

    public AssignmentRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long saveAssignment(String sapId, Assignment assignment) {
        return saveAssignment(sapId, assignment, assignment != null ? assignment.getAcademicPeriodId() : 0);
    }

    public long saveAssignment(String sapId, Assignment assignment, long academicPeriodId) {
        if (sapId == null || sapId.trim().isEmpty() || assignment == null || assignment.getTitle() == null || assignment.getSubject() == null) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ACCOUNT_SAP_ID, sapId.trim());
        values.put(DatabaseHelper.COL_ASSIGNMENT_TITLE, assignment.getTitle().trim());
        values.put(DatabaseHelper.COL_ASSIGNMENT_SUBJECT, assignment.getSubject().trim());
        values.put(DatabaseHelper.COL_ASSIGNMENT_DUE_DATE, assignment.getDueDate() != null ? assignment.getDueDate().trim() : "");
        values.put(DatabaseHelper.COL_ASSIGNMENT_IS_COMPLETED, assignment.isCompleted() ? 1 : 0);
        values.put(DatabaseHelper.COL_ASSIGNMENT_PRIORITY, assignment.getPriority());
        if (academicPeriodId > 0) values.put(DatabaseHelper.COL_ASSIGNMENT_PERIOD_ID, academicPeriodId);
        values.put(DatabaseHelper.COL_ASSIGNMENT_CREATED_AT, assignment.getCreatedAt() > 0 ? assignment.getCreatedAt() : now);
        values.put(DatabaseHelper.COL_ASSIGNMENT_UPDATED_AT, now);

        return db.insert(DatabaseHelper.TABLE_ASSIGNMENTS, null, values);
    }

    public boolean updateAssignmentCompletion(String sapId, long id, boolean isCompleted) {
        if (sapId == null || sapId.trim().isEmpty() || id <= 0) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ASSIGNMENT_IS_COMPLETED, isCompleted ? 1 : 0);
        values.put(DatabaseHelper.COL_ASSIGNMENT_UPDATED_AT, System.currentTimeMillis());

        int rows = db.update(
                DatabaseHelper.TABLE_ASSIGNMENTS,
                values,
                DatabaseHelper.COL_ASSIGNMENT_ID + " = ? AND " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?",
                new String[]{String.valueOf(id), sapId.trim()}
        );

        return rows > 0;
    }

    public boolean deleteAssignment(String sapId, long id) {
        if (sapId == null || sapId.trim().isEmpty() || id <= 0) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(
                DatabaseHelper.TABLE_ASSIGNMENTS,
                DatabaseHelper.COL_ASSIGNMENT_ID + " = ? AND " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?",
                new String[]{String.valueOf(id), sapId.trim()}
        );

        return rows > 0;
    }

    public List<Assignment> getAssignmentsForAccount(String sapId) {
        return getAssignmentsForPeriod(sapId, 0);
    }

    public List<Assignment> getAssignmentsForPeriod(String sapId, long periodId) {
        List<Assignment> list = new ArrayList<>();
        if (sapId == null || sapId.trim().isEmpty()) {
            return list;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_ASSIGNMENTS
                + " WHERE " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?"
                + (periodId > 0 ? " AND " + DatabaseHelper.COL_ASSIGNMENT_PERIOD_ID + " = ?" : "")
                + " ORDER BY " + DatabaseHelper.COL_ASSIGNMENT_IS_COMPLETED + " ASC, "
                + DatabaseHelper.COL_ASSIGNMENT_DUE_DATE + " ASC, "
                + DatabaseHelper.COL_ASSIGNMENT_CREATED_AT + " DESC";

        Cursor cursor = db.rawQuery(query, periodId > 0
                ? new String[]{sapId.trim(), String.valueOf(periodId)} : new String[]{sapId.trim()});
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    list.add(parseAssignmentFromCursor(cursor));
                }
            } finally {
                cursor.close();
            }
        }

        return list;
    }

    public List<Assignment> getIncompleteAssignmentsForAccount(String sapId) {
        return getIncompleteAssignmentsForPeriod(sapId, 0);
    }

    public List<Assignment> getIncompleteAssignmentsForPeriod(String sapId, long periodId) {
        List<Assignment> list = new ArrayList<>();
        if (sapId == null || sapId.trim().isEmpty()) {
            return list;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_ASSIGNMENTS
                + " WHERE " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ? AND "
                + DatabaseHelper.COL_ASSIGNMENT_IS_COMPLETED + " = 0"
                + (periodId > 0 ? " AND " + DatabaseHelper.COL_ASSIGNMENT_PERIOD_ID + " = ?" : "")
                + " ORDER BY " + DatabaseHelper.COL_ASSIGNMENT_DUE_DATE + " ASC, "
                + DatabaseHelper.COL_ASSIGNMENT_CREATED_AT + " DESC";

        Cursor cursor = db.rawQuery(query, periodId > 0
                ? new String[]{sapId.trim(), String.valueOf(periodId)} : new String[]{sapId.trim()});
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    list.add(parseAssignmentFromCursor(cursor));
                }
            } finally {
                cursor.close();
            }
        }

        return list;
    }

    public int getOpenAssignmentCountForAccount(String sapId) {
        return getOpenAssignmentCountForPeriod(sapId, 0);
    }

    public int getOpenAssignmentCountForPeriod(String sapId, long periodId) {
        if (sapId == null || sapId.trim().isEmpty()) {
            return 0;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ASSIGNMENTS
                + " WHERE " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ? AND "
                + DatabaseHelper.COL_ASSIGNMENT_IS_COMPLETED + " = 0"
                + (periodId > 0 ? " AND " + DatabaseHelper.COL_ASSIGNMENT_PERIOD_ID + " = ?" : "");

        Cursor cursor = db.rawQuery(query, periodId > 0
                ? new String[]{sapId.trim(), String.valueOf(periodId)} : new String[]{sapId.trim()});
        int count = 0;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                }
            } finally {
                cursor.close();
            }
        }
        return count;
    }

    private Assignment parseAssignmentFromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ASSIGNMENT_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ASSIGNMENT_TITLE));
        String subject = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ASSIGNMENT_SUBJECT));
        String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ASSIGNMENT_DUE_DATE));
        int isCompletedInt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ASSIGNMENT_IS_COMPLETED));
        String priority = "MEDIUM";
        int priorityIdx = cursor.getColumnIndex(DatabaseHelper.COL_ASSIGNMENT_PRIORITY);
        if (priorityIdx != -1) {
            priority = cursor.getString(priorityIdx);
        }
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ASSIGNMENT_CREATED_AT));
        long updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ASSIGNMENT_UPDATED_AT));
        long periodId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ASSIGNMENT_PERIOD_ID));

        return new Assignment(id, title, subject, dueDate, isCompletedInt == 1, priority, periodId, createdAt, updatedAt);
    }
}
