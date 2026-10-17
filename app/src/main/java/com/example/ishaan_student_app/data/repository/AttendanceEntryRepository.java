package com.example.ishaan_student_app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ishaan_student_app.data.db.DatabaseHelper;
import com.example.ishaan_student_app.data.model.AttendanceEntry;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AttendanceEntryRepository {

    private final DatabaseHelper dbHelper;

    @Inject
    public AttendanceEntryRepository(@ApplicationContext Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long saveOrUpdateEntry(AttendanceEntry entry) {
        if (entry == null || entry.getOwnerAccountId() == null || entry.getOwnerAccountId().trim().isEmpty() || entry.getSubjectId() <= 0 || entry.getClassDate() == null || entry.getClassDate().trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (!subjectBelongsToAccount(db, entry.getOwnerAccountId().trim(), entry.getSubjectId())) {
            return -1;
        }
        long now = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ACCOUNT_SAP_ID, entry.getOwnerAccountId().trim());
        values.put(DatabaseHelper.COL_ENTRY_SUBJ_ID, entry.getSubjectId());
        values.put(DatabaseHelper.COL_ENTRY_CLASS_DATE, entry.getClassDate().trim());
        values.put(DatabaseHelper.COL_ENTRY_CLASS_TIME, entry.getClassTime() != null ? entry.getClassTime().trim() : "");
        values.put(DatabaseHelper.COL_ENTRY_OCCURRENCE, entry.getOccurrenceLabel() != null ? entry.getOccurrenceLabel().trim() : "");
        values.put(DatabaseHelper.COL_ENTRY_STATUS, entry.getStatus() != null ? entry.getStatus().trim().toUpperCase(Locale.ROOT) : "PRESENT");
        values.put(DatabaseHelper.COL_ENTRY_NOTE, entry.getNote() != null ? entry.getNote().trim() : "");
        values.put(DatabaseHelper.COL_ENTRY_CREATED_AT, entry.getCreatedAt() > 0 ? entry.getCreatedAt() : now);
        values.put(DatabaseHelper.COL_ENTRY_UPDATED_AT, now);

        if (entry.getId() > 0) {
            int rows = db.update(DatabaseHelper.TABLE_ATTENDANCE_ENTRIES, values,
                    DatabaseHelper.COL_ENTRY_ID + " = ? AND " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?",
                    new String[]{String.valueOf(entry.getId()), entry.getOwnerAccountId().trim()});
            return rows > 0 ? entry.getId() : -1;
        } else {
            return db.insert(DatabaseHelper.TABLE_ATTENDANCE_ENTRIES, null, values);
        }
    }

    public List<AttendanceEntry> getEntriesForSubject(String sapId, long subjectId) {
        List<AttendanceEntry> list = new ArrayList<>();
        if (sapId == null || sapId.trim().isEmpty() || subjectId <= 0) {
            return list;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_ATTENDANCE_ENTRIES
                + " WHERE " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ? AND " + DatabaseHelper.COL_ENTRY_SUBJ_ID + " = ?"
                + " ORDER BY " + DatabaseHelper.COL_ENTRY_CREATED_AT + " DESC, " + DatabaseHelper.COL_ENTRY_ID + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{sapId.trim(), String.valueOf(subjectId)});
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    list.add(parseEntryFromCursor(cursor));
                }
            } finally {
                cursor.close();
            }
        }

        return list;
    }

    public boolean deleteEntry(String sapId, long entryId) {
        if (sapId == null || sapId.trim().isEmpty() || entryId <= 0) {
            return false;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_ATTENDANCE_ENTRIES,
                DatabaseHelper.COL_ENTRY_ID + " = ? AND " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?",
                new String[]{String.valueOf(entryId), sapId.trim()});
        return rows > 0;
    }

    private boolean subjectBelongsToAccount(SQLiteDatabase db, String sapId, long subjectId) {
        String query = "SELECT 1 FROM " + DatabaseHelper.TABLE_PERSONAL_SUBJECTS
                + " WHERE " + DatabaseHelper.COL_SUBJ_ID + " = ? AND " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ? LIMIT 1";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(subjectId), sapId})) {
            return cursor.moveToFirst();
        }
    }

    public int getPresentCountForSubject(String sapId, long subjectId) {
        return getStatusCount(sapId, subjectId, "PRESENT");
    }

    public int getAbsentCountForSubject(String sapId, long subjectId) {
        return getStatusCount(sapId, subjectId, "ABSENT");
    }

    public int getCancelledCountForSubject(String sapId, long subjectId) {
        return getStatusCount(sapId, subjectId, "CANCELLED");
    }

    private int getStatusCount(String sapId, long subjectId, String status) {
        if (sapId == null || sapId.trim().isEmpty() || subjectId <= 0) {
            return 0;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ATTENDANCE_ENTRIES
                + " WHERE " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ? AND "
                + DatabaseHelper.COL_ENTRY_SUBJ_ID + " = ? AND "
                + DatabaseHelper.COL_ENTRY_STATUS + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{sapId.trim(), String.valueOf(subjectId), status});
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

    private AttendanceEntry parseEntryFromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ENTRY_ID));
        String owner = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACCOUNT_SAP_ID));
        long subjectId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ENTRY_SUBJ_ID));
        String classDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ENTRY_CLASS_DATE));
        String classTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ENTRY_CLASS_TIME));
        String occurrence = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ENTRY_OCCURRENCE));
        String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ENTRY_STATUS));
        String note = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ENTRY_NOTE));
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ENTRY_CREATED_AT));
        long updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ENTRY_UPDATED_AT));

        return new AttendanceEntry(id, owner, subjectId, classDate, classTime, occurrence, status, note, createdAt, updatedAt);
    }
}
