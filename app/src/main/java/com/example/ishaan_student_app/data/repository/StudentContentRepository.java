package com.example.ishaan_student_app.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ishaan_student_app.data.db.DatabaseHelper;
import com.example.ishaan_student_app.data.model.StudentEvent;
import com.example.ishaan_student_app.data.model.UserResource;

import java.util.ArrayList;
import java.util.List;

public class StudentContentRepository {
    private final DatabaseHelper database;

    public StudentContentRepository(Context context) {
        database = DatabaseHelper.getInstance(context.getApplicationContext());
    }

    public long addResource(String sapId, String title, String mimeType, String uri) {
        return addResource(sapId, title, mimeType, uri, "General");
    }

    public long addResource(String sapId, String title, String mimeType, String uri, String subjectName) {
        if (isBlank(sapId) || isBlank(title) || isBlank(mimeType) || isBlank(uri)) return -1;
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ACCOUNT_SAP_ID, sapId.trim());
        values.put("title", title.trim());
        values.put("mime_type", mimeType.trim());
        values.put("uri", uri.trim());
        values.put("subject_name", isBlank(subjectName) ? "General" : subjectName.trim());
        values.put("created_at", System.currentTimeMillis());
        return database.getWritableDatabase().insertWithOnConflict(
                DatabaseHelper.TABLE_USER_RESOURCES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public List<UserResource> getResources(String sapId) {
        return getResourcesBySubject(sapId, null);
    }

    public List<UserResource> getResourcesBySubject(String sapId, String subjectFilter) {
        List<UserResource> items = new ArrayList<>();
        if (isBlank(sapId)) return items;

        boolean filterBySubject = !isBlank(subjectFilter) && !"All Subjects".equalsIgnoreCase(subjectFilter.trim());
        String query = "SELECT id, title, mime_type, uri, subject_name FROM " + DatabaseHelper.TABLE_USER_RESOURCES
                + " WHERE " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?"
                + (filterBySubject ? " AND subject_name = ? COLLATE NOCASE" : "")
                + " ORDER BY created_at DESC, id DESC";

        String[] selectionArgs = filterBySubject
                ? new String[]{sapId.trim(), subjectFilter.trim()}
                : new String[]{sapId.trim()};

        try (Cursor cursor = database.getReadableDatabase().rawQuery(query, selectionArgs)) {
            while (cursor.moveToNext()) {
                String subjectName = cursor.getColumnCount() > 4 ? cursor.getString(4) : "General";
                items.add(new UserResource(
                        cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), subjectName));
            }
        } catch (Exception e) {
            // Fallback for older database instances without subject_name column
            try (Cursor cursor = database.getReadableDatabase().rawQuery(
                    "SELECT id, title, mime_type, uri FROM " + DatabaseHelper.TABLE_USER_RESOURCES
                            + " WHERE " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ? ORDER BY created_at DESC, id DESC",
                    new String[]{sapId.trim()})) {
                while (cursor.moveToNext()) {
                    items.add(new UserResource(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), "General"));
                }
            } catch (Exception ignored) {
            }
        }
        return items;
    }

    public boolean deleteResource(String sapId, long id) {
        if (isBlank(sapId) || id <= 0) return false;
        return database.getWritableDatabase().delete(DatabaseHelper.TABLE_USER_RESOURCES,
                "id = ? AND " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?",
                new String[]{String.valueOf(id), sapId.trim()}) > 0;
    }

    public long addEvent(String sapId, String title, String date, String venue, String details, String imageUri) {
        if (isBlank(sapId) || isBlank(title) || isBlank(date) || isBlank(venue)) return -1;
        long now = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ACCOUNT_SAP_ID, sapId.trim());
        values.put("title", title.trim());
        values.put("event_date", date.trim());
        values.put("venue", venue.trim());
        values.put("details", details == null ? "" : details.trim());
        values.put("image_uri", imageUri == null ? "" : imageUri.trim());
        values.put("created_at", now);
        values.put("updated_at", now);
        return database.getWritableDatabase().insert(DatabaseHelper.TABLE_EVENTS, null, values);
    }

    public List<StudentEvent> getEvents(String sapId) {
        List<StudentEvent> items = new ArrayList<>();
        if (isBlank(sapId)) return items;
        try (Cursor cursor = database.getReadableDatabase().rawQuery(
                "SELECT id,title,event_date,venue,details,image_uri FROM " + DatabaseHelper.TABLE_EVENTS
                        + " WHERE " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ? ORDER BY event_date ASC, id DESC",
                new String[]{sapId.trim()})) {
            while (cursor.moveToNext()) items.add(new StudentEvent(cursor.getLong(0), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)));
        }
        return items;
    }

    public boolean deleteEvent(String sapId, long id) {
        if (isBlank(sapId) || id <= 0) return false;
        return database.getWritableDatabase().delete(DatabaseHelper.TABLE_EVENTS,
                "id = ? AND " + DatabaseHelper.COL_ACCOUNT_SAP_ID + " = ?",
                new String[]{String.valueOf(id), sapId.trim()}) > 0;
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
}
