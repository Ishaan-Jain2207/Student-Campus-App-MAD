package com.example.ishaan_student_app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ishaan_student_app.data.db.DatabaseHelper;
import com.example.ishaan_student_app.data.model.PersonalSubject;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

public class PersonalSubjectRepository {

    private final DatabaseHelper dbHelper;

    @Inject
    public PersonalSubjectRepository(@ApplicationContext Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long saveOrUpdateSubject(PersonalSubject subject) {
        if (subject == null || subject.getOwnerAccountId() == null || subject.getOwnerAccountId().trim().isEmpty() || subject.getSubjectName() == null || subject.getSubjectName().trim().isEmpty() || subject.getAcademicPeriodId() <= 0) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (!periodBelongsToAccount(db, subject.getOwnerAccountId().trim(), subject.getAcademicPeriodId())) {
            return -1;
        }
        long now = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ACCOUNT_SAP_ID, subject.getOwnerAccountId().trim());
        values.put(DatabaseHelper.COL_SUBJ_PERIOD_ID, subject.getAcademicPeriodId());
        values.put(DatabaseHelper.COL_SUBJ_NAME, subject.getSubjectName().trim());
        values.put(DatabaseHelper.COL_SUBJ_CODE, subject.getSubjectCode() != null ? subject.getSubjectCode().trim() : "");
        values.put(DatabaseHelper.COL_SUBJ_IS_ARCHIVED, subject.isArchived() ? 1 : 0);
        values.put(DatabaseHelper.COL_SUBJ_CREATED_AT, subject.getCreatedAt() > 0 ? subject.getCreatedAt() : now);
        values.put(DatabaseHelper.COL_SUBJ_UPDATED_AT, now);

        if (subject.getId() > 0) {
            int rows = db.update(DatabaseHelper.TABLE_PERSONAL_SUBJECTS, values,
                    DatabaseHelper.COL_SUBJ_ID + " = ? AND " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?",
                    new String[]{String.valueOf(subject.getId()), subject.getOwnerAccountId().trim()});
            return rows > 0 ? subject.getId() : -1;
        } else {
            return db.insert(DatabaseHelper.TABLE_PERSONAL_SUBJECTS, null, values);
        }
    }

    public List<PersonalSubject> getSubjectsForPeriod(String sapId, long periodId) {
        List<PersonalSubject> list = new ArrayList<>();
        if (sapId == null || sapId.trim().isEmpty() || periodId <= 0) {
            return list;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_PERSONAL_SUBJECTS
                + " WHERE " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ? AND " + DatabaseHelper.COL_SUBJ_PERIOD_ID + " = ?"
                + " ORDER BY " + DatabaseHelper.COL_SUBJ_NAME + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{sapId.trim(), String.valueOf(periodId)});
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    list.add(parseSubjectFromCursor(cursor));
                }
            } finally {
                cursor.close();
            }
        }

        return list;
    }

    public boolean deleteSubject(String sapId, long subjectId) {
        if (sapId == null || sapId.trim().isEmpty() || subjectId <= 0) {
            return false;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_PERSONAL_SUBJECTS,
                DatabaseHelper.COL_SUBJ_ID + " = ? AND " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?",
                new String[]{String.valueOf(subjectId), sapId.trim()});
        return rows > 0;
    }

    private boolean periodBelongsToAccount(SQLiteDatabase db, String sapId, long periodId) {
        String query = "SELECT 1 FROM " + DatabaseHelper.TABLE_ACADEMIC_PERIODS
                + " WHERE " + DatabaseHelper.COL_PERIOD_ID + " = ? AND " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ? LIMIT 1";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(periodId), sapId})) {
            return cursor.moveToFirst();
        }
    }

    private PersonalSubject parseSubjectFromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SUBJ_ID));
        String owner = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACCOUNT_SAP_ID));
        long periodId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SUBJ_PERIOD_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SUBJ_NAME));
        String code = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SUBJ_CODE));
        int isArchivedInt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SUBJ_IS_ARCHIVED));
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SUBJ_CREATED_AT));
        long updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SUBJ_UPDATED_AT));

        return new PersonalSubject(id, owner, periodId, name, code, isArchivedInt == 1, createdAt, updatedAt);
    }
}
