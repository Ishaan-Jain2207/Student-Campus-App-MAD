# CampusMate data layer

This document describes the current Android source, not a proposed backend contract.

## Local database

- File: `campusmate.db`
- Helper: `DatabaseHelper`
- Schema version: `8`
- Persistence: SQLite. There is no remote sync client in this project.

### Tables

| Table | Purpose | Account scope |
|---|---|---|
| `students` | Student profile fields and photo URI created during local registration | SAP ID is the profile key |
| `local_accounts` | Salted PBKDF2 password verifier and email for device-local authentication | Unique SAP ID and email; linked to `students` |
| `academic_periods` | Student-created academic years and semesters | `owner_account_id` |
| `personal_subjects` | Student-created subjects within one semester | `owner_account_id` |
| `attendance_entries` | Manually recorded PRESENT, ABSENT, or CANCELLED class sessions | `owner_account_id` |
| `assignments` | Student-created assignment title, subject label, due date, and completion | `owner_account_id` |

Attendance belongs to a personal semester and subject. CANCELLED entries are excluded from the conducted-session denominator. Deleting a semester cascades to its subjects and sessions. Repositories also check that the parent period or subject belongs to the requested account before writes.

Assignments are account-scoped but currently store a subject label rather than a semester/subject foreign key. The dialog offers subjects from the active semester; historical assignments do not retain a semester relationship.

`onUpgrade` creates missing personal attendance tables and the local account table idempotently. It does not drop or recreate existing tables.

## Attendance calculations

For a subject, `present / (present + absent) × 100` is used. This is a student-maintained personal record, not a university record. The app displays advisory targets of 80% for STME/SOC and 70% for SOL. Unknown departments do not receive a default target.

## Repositories

- `StudentRepository`: local student profile lookup/update.
- `AcademicPeriodRepository`: account-scoped semester create, selection, and deletion.
- `PersonalSubjectRepository`: subjects for a student-created semester.
- `AttendanceEntryRepository`: account-scoped personal session CRUD and counts.
- `AssignmentRepository`: account-scoped assignment creation, completion toggles, deletion, listing, and counts.
- `CampusLocationRepository`: one NMIMS Chandigarh campus destination.
- `ResourceFileReader`: reads the curated `campus_resources.csv` asset.

## Authentication and synchronization status

Registration creates a device-local profile and account in one SQLite transaction. Passwords are never stored as plaintext: the account table stores a unique random salt and PBKDF2-HMAC-SHA1 verifier. Sign-in checks SAP ID, email, and password locally. The `@nmims.in` suffix is format-checked only; it does not verify identity or enrollment. Forgot Password cannot safely reset an offline account without proving email ownership, so it explains that reset is unavailable. There is no backend client or sync worker; local accounts and their personal data do not sync across devices.

See [API_GAPS.md](API_GAPS.md) for what is still required to add verified campus accounts and remote synchronization.
