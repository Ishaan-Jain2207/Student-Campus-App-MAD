package com.example.ishaan_student_app.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Native SQLiteOpenHelper implementation for SVKM's NMIMS Chandigarh CampusMate.
 * Manages account-scoped tables for Students, Academic Periods, Personal Subjects, Attendance Entries, Assignments, Resources, and Events.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "campusmate.db";
    private static final int DATABASE_VERSION = 12;

    // Singleton Instance
    private static DatabaseHelper instance;

    // Table Names
    public static final String TABLE_STUDENTS = "students";
    public static final String TABLE_ASSIGNMENTS = "assignments";

    public static final String TABLE_ACADEMIC_PERIODS = "academic_periods";
    public static final String TABLE_PERSONAL_SUBJECTS = "personal_subjects";
    public static final String TABLE_ATTENDANCE_ENTRIES = "attendance_entries";
    public static final String TABLE_LOCAL_ACCOUNTS = "local_accounts";
    public static final String TABLE_USER_RESOURCES = "user_resources";
    public static final String TABLE_EVENTS = "student_events";

    // Students Table Columns
    public static final String COL_STUDENT_ID = "id";
    public static final String COL_STUDENT_SAP_ID = "sap_id";
    public static final String COL_STUDENT_NAME = "name";
    public static final String COL_STUDENT_EMAIL = "email";
    public static final String COL_STUDENT_DEPT = "department";
    public static final String COL_STUDENT_YEAR = "year";
    public static final String COL_STUDENT_PHOTO_URI = "photo_uri";
    public static final String COL_AUTH_SALT = "password_salt";
    public static final String COL_AUTH_HASH = "password_hash";

    // Account Scoping Key
    public static final String COL_ACCOUNT_SAP_ID = "owner_account_id";

    // Academic Periods Table Columns
    public static final String COL_PERIOD_ID = "id";
    public static final String COL_PERIOD_YEAR = "academic_year";
    public static final String COL_PERIOD_SEMESTER = "semester_label";
    public static final String COL_PERIOD_START_DATE = "start_date";
    public static final String COL_PERIOD_END_DATE = "end_date";
    public static final String COL_PERIOD_IS_ACTIVE = "is_active";
    public static final String COL_PERIOD_IS_ARCHIVED = "is_archived";
    public static final String COL_PERIOD_CREATED_AT = "created_at";
    public static final String COL_PERIOD_UPDATED_AT = "updated_at";

    // Personal Subjects Table Columns
    public static final String COL_SUBJ_ID = "id";
    public static final String COL_SUBJ_PERIOD_ID = "academic_period_id";
    public static final String COL_SUBJ_NAME = "subject_name";
    public static final String COL_SUBJ_CODE = "subject_code";
    public static final String COL_SUBJ_IS_ARCHIVED = "is_archived";
    public static final String COL_SUBJ_CREATED_AT = "created_at";
    public static final String COL_SUBJ_UPDATED_AT = "updated_at";

    // Attendance Entries Table Columns
    public static final String COL_ENTRY_ID = "id";
    public static final String COL_ENTRY_SUBJ_ID = "subject_id";
    public static final String COL_ENTRY_CLASS_DATE = "class_date";
    public static final String COL_ENTRY_CLASS_TIME = "class_time";
    public static final String COL_ENTRY_OCCURRENCE = "occurrence_label";
    public static final String COL_ENTRY_STATUS = "status";
    public static final String COL_ENTRY_NOTE = "note";
    public static final String COL_ENTRY_CREATED_AT = "created_at";
    public static final String COL_ENTRY_UPDATED_AT = "updated_at";

    // Assignments Table Columns
    public static final String COL_ASSIGNMENT_ID = "id";
    public static final String COL_ASSIGNMENT_TITLE = "title";
    public static final String COL_ASSIGNMENT_SUBJECT = "subject";
    public static final String COL_ASSIGNMENT_DUE_DATE = "due_date";
    public static final String COL_ASSIGNMENT_IS_COMPLETED = "is_completed";
    public static final String COL_ASSIGNMENT_PRIORITY = "priority";
    public static final String COL_ASSIGNMENT_CREATED_AT = "created_at";
    public static final String COL_ASSIGNMENT_UPDATED_AT = "updated_at";
    public static final String COL_ASSIGNMENT_PERIOD_ID = "academic_period_id";

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Students Table
        String createStudentsTable = "CREATE TABLE " + TABLE_STUDENTS + " ("
                + COL_STUDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_STUDENT_SAP_ID + " TEXT NOT NULL UNIQUE, "
                + COL_STUDENT_NAME + " TEXT NOT NULL, "
                + COL_STUDENT_EMAIL + " TEXT NOT NULL UNIQUE, "
                + COL_STUDENT_DEPT + " TEXT NOT NULL, "
                + COL_STUDENT_YEAR + " INTEGER NOT NULL, "
                + COL_STUDENT_PHOTO_URI + " TEXT);";
        db.execSQL(createStudentsTable);
        createLocalAccountsTable(db);

        createPersonalAttendanceTables(db);

        // Create Assignments Table (Scoped to owner_account_id)
        String createAssignmentsTable = "CREATE TABLE " + TABLE_ASSIGNMENTS + " ("
                + COL_ASSIGNMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ACCOUNT_SAP_ID + " TEXT NOT NULL, "
                + COL_ASSIGNMENT_TITLE + " TEXT NOT NULL, "
                + COL_ASSIGNMENT_SUBJECT + " TEXT NOT NULL, "
                + COL_ASSIGNMENT_DUE_DATE + " TEXT NOT NULL, "
                + COL_ASSIGNMENT_IS_COMPLETED + " INTEGER NOT NULL DEFAULT 0, "
                + COL_ASSIGNMENT_PRIORITY + " TEXT NOT NULL DEFAULT 'MEDIUM', "
                + COL_ASSIGNMENT_PERIOD_ID + " INTEGER REFERENCES " + TABLE_ACADEMIC_PERIODS
                + "(" + COL_PERIOD_ID + ") ON DELETE CASCADE, "
                + COL_ASSIGNMENT_CREATED_AT + " INTEGER NOT NULL, "
                + COL_ASSIGNMENT_UPDATED_AT + " INTEGER NOT NULL);";
        db.execSQL(createAssignmentsTable);
        createStudentContentTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            // Remove legacy seeded identity and fake subjects
            db.execSQL("DELETE FROM " + TABLE_STUDENTS + " WHERE " + COL_STUDENT_SAP_ID + " = '700112233'");

            try {
                db.execSQL("ALTER TABLE " + TABLE_ASSIGNMENTS + " ADD COLUMN " + COL_ACCOUNT_SAP_ID + " TEXT NOT NULL DEFAULT ''");
            } catch (Exception ignored) {
            }

            // Quarantine or remove unowned assignments (owner_account_id = '')
            db.execSQL("DELETE FROM " + TABLE_ASSIGNMENTS + " WHERE " + COL_ACCOUNT_SAP_ID + " = ''");
        }

        if (oldVersion < 7) {
            createPersonalAttendanceTables(db);
        }

        if (oldVersion < 8) {
            createLocalAccountsTable(db);
        }

        if (oldVersion < 9) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_ASSIGNMENTS + " ADD COLUMN "
                        + COL_ASSIGNMENT_PERIOD_ID + " INTEGER REFERENCES " + TABLE_ACADEMIC_PERIODS
                        + "(" + COL_PERIOD_ID + ") ON DELETE CASCADE");
            } catch (Exception ignored) {
            }
            createStudentContentTables(db);
        }

        if (oldVersion < 11) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_ASSIGNMENTS + " ADD COLUMN "
                        + COL_ASSIGNMENT_PRIORITY + " TEXT NOT NULL DEFAULT 'MEDIUM'");
            } catch (Exception ignored) {
            }
        }

        if (oldVersion < 12) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USER_RESOURCES + " ADD COLUMN subject_name TEXT NOT NULL DEFAULT 'General'");
            } catch (Exception ignored) {
            }
        }
    }

    private void createStudentContentTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USER_RESOURCES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_ACCOUNT_SAP_ID + " TEXT NOT NULL, "
                + "title TEXT NOT NULL, mime_type TEXT NOT NULL, uri TEXT NOT NULL, "
                + "subject_name TEXT NOT NULL DEFAULT 'General', created_at INTEGER NOT NULL, "
                + "UNIQUE(" + COL_ACCOUNT_SAP_ID + ", uri))");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_ACCOUNT_SAP_ID + " TEXT NOT NULL, "
                + "title TEXT NOT NULL, event_date TEXT NOT NULL, venue TEXT NOT NULL, details TEXT, image_uri TEXT, "
                + "created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)");
    }

    private void createLocalAccountsTable(SQLiteDatabase db) {
        String createLocalAccountsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_LOCAL_ACCOUNTS + " ("
                + COL_STUDENT_SAP_ID + " TEXT PRIMARY KEY, "
                + COL_STUDENT_EMAIL + " TEXT NOT NULL COLLATE NOCASE UNIQUE, "
                + COL_AUTH_SALT + " BLOB NOT NULL, "
                + COL_AUTH_HASH + " BLOB NOT NULL, "
                + "created_at INTEGER NOT NULL, "
                + "FOREIGN KEY(" + COL_STUDENT_SAP_ID + ") REFERENCES " + TABLE_STUDENTS
                + "(" + COL_STUDENT_SAP_ID + ") ON DELETE CASCADE);";
        db.execSQL(createLocalAccountsTable);
    }

    private void createPersonalAttendanceTables(SQLiteDatabase db) {
        String createAcademicPeriodsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_ACADEMIC_PERIODS + " ("
                + COL_PERIOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ACCOUNT_SAP_ID + " TEXT NOT NULL, "
                + COL_PERIOD_YEAR + " TEXT NOT NULL, "
                + COL_PERIOD_SEMESTER + " TEXT NOT NULL, "
                + COL_PERIOD_START_DATE + " TEXT, "
                + COL_PERIOD_END_DATE + " TEXT, "
                + COL_PERIOD_IS_ACTIVE + " INTEGER NOT NULL DEFAULT 0, "
                + COL_PERIOD_IS_ARCHIVED + " INTEGER NOT NULL DEFAULT 0, "
                + COL_PERIOD_CREATED_AT + " INTEGER NOT NULL, "
                + COL_PERIOD_UPDATED_AT + " INTEGER NOT NULL, "
                + "UNIQUE(" + COL_ACCOUNT_SAP_ID + ", " + COL_PERIOD_YEAR + ", " + COL_PERIOD_SEMESTER + ") ON CONFLICT FAIL);";
        db.execSQL(createAcademicPeriodsTable);

        String createPersonalSubjectsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PERSONAL_SUBJECTS + " ("
                + COL_SUBJ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ACCOUNT_SAP_ID + " TEXT NOT NULL, "
                + COL_SUBJ_PERIOD_ID + " INTEGER NOT NULL, "
                + COL_SUBJ_NAME + " TEXT NOT NULL, "
                + COL_SUBJ_CODE + " TEXT, "
                + COL_SUBJ_IS_ARCHIVED + " INTEGER NOT NULL DEFAULT 0, "
                + COL_SUBJ_CREATED_AT + " INTEGER NOT NULL, "
                + COL_SUBJ_UPDATED_AT + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + COL_SUBJ_PERIOD_ID + ") REFERENCES " + TABLE_ACADEMIC_PERIODS + "(" + COL_PERIOD_ID + ") ON DELETE CASCADE);";
        db.execSQL(createPersonalSubjectsTable);

        String createAttendanceEntriesTable = "CREATE TABLE IF NOT EXISTS " + TABLE_ATTENDANCE_ENTRIES + " ("
                + COL_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ACCOUNT_SAP_ID + " TEXT NOT NULL, "
                + COL_ENTRY_SUBJ_ID + " INTEGER NOT NULL, "
                + COL_ENTRY_CLASS_DATE + " TEXT NOT NULL, "
                + COL_ENTRY_CLASS_TIME + " TEXT, "
                + COL_ENTRY_OCCURRENCE + " TEXT, "
                + COL_ENTRY_STATUS + " TEXT NOT NULL, "
                + COL_ENTRY_NOTE + " TEXT, "
                + COL_ENTRY_CREATED_AT + " INTEGER NOT NULL, "
                + COL_ENTRY_UPDATED_AT + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + COL_ENTRY_SUBJ_ID + ") REFERENCES " + TABLE_PERSONAL_SUBJECTS + "(" + COL_SUBJ_ID + ") ON DELETE CASCADE);";
        db.execSQL(createAttendanceEntriesTable);
    }
}
