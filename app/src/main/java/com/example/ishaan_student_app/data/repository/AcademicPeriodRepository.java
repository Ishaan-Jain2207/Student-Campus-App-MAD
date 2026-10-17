package com.example.ishaan_student_app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ishaan_student_app.data.db.DatabaseHelper;
import com.example.ishaan_student_app.data.model.AcademicPeriod;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

public class AcademicPeriodRepository {

    private final DatabaseHelper dbHelper;

    @Inject
    public AcademicPeriodRepository(@ApplicationContext Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long saveOrUpdatePeriod(AcademicPeriod period) {
        if (period == null || period.getOwnerAccountId() == null || period.getOwnerAccountId().trim().isEmpty() || period.getSemesterLabel() == null || period.getSemesterLabel().trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long now = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ACCOUNT_SAP_ID, period.getOwnerAccountId().trim());
        values.put(DatabaseHelper.COL_PERIOD_YEAR, period.getAcademicYear() != null ? period.getAcademicYear().trim() : "");
        values.put(DatabaseHelper.COL_PERIOD_SEMESTER, period.getSemesterLabel().trim());
        values.put(DatabaseHelper.COL_PERIOD_START_DATE, period.getStartDate() != null ? period.getStartDate().trim() : "");
        values.put(DatabaseHelper.COL_PERIOD_END_DATE, period.getEndDate() != null ? period.getEndDate().trim() : "");
        values.put(DatabaseHelper.COL_PERIOD_IS_ACTIVE, period.isActive() ? 1 : 0);
        values.put(DatabaseHelper.COL_PERIOD_IS_ARCHIVED, period.isArchived() ? 1 : 0);
        values.put(DatabaseHelper.COL_PERIOD_UPDATED_AT, now);

        if (period.getId() > 0) {
            int rows = db.update(DatabaseHelper.TABLE_ACADEMIC_PERIODS, values,
                    DatabaseHelper.COL_PERIOD_ID + " = ? AND " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?",
                    new String[]{String.valueOf(period.getId()), period.getOwnerAccountId().trim()});
            return rows > 0 ? period.getId() : -1;
        }

        String year = period.getAcademicYear() != null ? period.getAcademicYear().trim() : "";
        long existingId = findPeriodId(db, period.getOwnerAccountId().trim(), year, period.getSemesterLabel().trim());
        if (existingId > 0) {
            int rows = db.update(DatabaseHelper.TABLE_ACADEMIC_PERIODS, values,
                    DatabaseHelper.COL_PERIOD_ID + " = ? AND " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?",
                    new String[]{String.valueOf(existingId), period.getOwnerAccountId().trim()});
            return rows > 0 ? existingId : -1;
        }

        values.put(DatabaseHelper.COL_PERIOD_CREATED_AT, period.getCreatedAt() > 0 ? period.getCreatedAt() : now);
        return db.insert(DatabaseHelper.TABLE_ACADEMIC_PERIODS, null, values);
    }

    private long findPeriodId(SQLiteDatabase db, String sapId, String year, String semester) {
        String query = "SELECT " + DatabaseHelper.COL_PERIOD_ID + " FROM " + DatabaseHelper.TABLE_ACADEMIC_PERIODS
                + " WHERE " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ? AND "
                + DatabaseHelper.COL_PERIOD_YEAR + " = ? AND " + DatabaseHelper.COL_PERIOD_SEMESTER + " = ? LIMIT 1";
        try (Cursor cursor = db.rawQuery(query, new String[]{sapId, year, semester})) {
            return cursor.moveToFirst() ? cursor.getLong(0) : -1;
        }
    }

    public List<AcademicPeriod> getPeriodsForAccount(String sapId) {
        List<AcademicPeriod> list = new ArrayList<>();
        if (sapId == null || sapId.trim().isEmpty()) {
            return list;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_ACADEMIC_PERIODS
                + " WHERE " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?"
                + " ORDER BY " + DatabaseHelper.COL_PERIOD_IS_ACTIVE + " DESC, "
                + DatabaseHelper.COL_PERIOD_ID + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{sapId.trim()});
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    list.add(parsePeriodFromCursor(cursor));
                }
            } finally {
                cursor.close();
            }
        }

        return list;
    }

    public AcademicPeriod getActivePeriodForAccount(String sapId) {
        if (sapId == null || sapId.trim().isEmpty()) {
            return null;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        AcademicPeriod period = null;

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_ACADEMIC_PERIODS
                + " WHERE " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ? AND "
                + DatabaseHelper.COL_PERIOD_IS_ACTIVE + " = 1 AND "
                + DatabaseHelper.COL_PERIOD_IS_ARCHIVED + " = 0"
                + " LIMIT 1";

        Cursor cursor = db.rawQuery(query, new String[]{sapId.trim()});
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    period = parsePeriodFromCursor(cursor);
                }
            } finally {
                cursor.close();
            }
        }

        if (period == null) {
            List<AcademicPeriod> allPeriods = getPeriodsForAccount(sapId);
            for (AcademicPeriod candidate : allPeriods) {
                if (!candidate.isArchived()) {
                    period = candidate;
                    break;
                }
            }
        }

        return period;
    }

    public boolean setActivePeriod(String sapId, long periodId) {
        if (sapId == null || sapId.trim().isEmpty() || periodId <= 0) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues clearValues = new ContentValues();
            clearValues.put(DatabaseHelper.COL_PERIOD_IS_ACTIVE, 0);
            db.update(DatabaseHelper.TABLE_ACADEMIC_PERIODS, clearValues, DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?", new String[]{sapId.trim()});

            ContentValues activeValues = new ContentValues();
            activeValues.put(DatabaseHelper.COL_PERIOD_IS_ACTIVE, 1);
            int rows = db.update(DatabaseHelper.TABLE_ACADEMIC_PERIODS, activeValues,
                    DatabaseHelper.COL_PERIOD_ID + " = ? AND " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?",
                    new String[]{String.valueOf(periodId), sapId.trim()});
            if (rows <= 0) return false;
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public boolean deletePeriod(String sapId, long periodId) {
        if (sapId == null || sapId.trim().isEmpty() || periodId <= 0) {
            return false;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_ACADEMIC_PERIODS,
                DatabaseHelper.COL_PERIOD_ID + " = ? AND " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?",
                new String[]{String.valueOf(periodId), sapId.trim()});
        return rows > 0;
    }

    private AcademicPeriod parsePeriodFromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PERIOD_ID));
        String owner = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACCOUNT_SAP_ID));
        String year = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PERIOD_YEAR));
        String semester = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PERIOD_SEMESTER));
        String startDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PERIOD_START_DATE));
        String endDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PERIOD_END_DATE));
        int isActiveInt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PERIOD_IS_ACTIVE));
        int isArchivedInt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PERIOD_IS_ARCHIVED));
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PERIOD_CREATED_AT));
        long updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PERIOD_UPDATED_AT));

        return new AcademicPeriod(id, owner, year, semester, startDate, endDate, isActiveInt == 1, isArchivedInt == 1, createdAt, updatedAt);
    }
}
