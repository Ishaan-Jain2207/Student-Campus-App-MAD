# CampusMate architecture snapshot

CampusMate is an Android app for NMIMS Chandigarh. Student attendance, semester, subject, assignment, and locally cached profile data are stored on-device.

## Application layers

```text
Activities and fragments
  Splash · Login · Register · Home · Attendance · Assignments
  Campus Map · Profile · Resources
            ↓
Repositories and utilities
  Student · Academic Period · Personal Subject · Attendance Entry · Assignment
  Campus Location · Resource File Reader · Session Manager
            ↓
SQLite, SharedPreferences, bundled campus_resources.csv, Google Maps SDK
```

Primary navigation is Home, Attendance, Assignments, Campus, and Profile. The drawer contains Resources and Sign out. There is no events feed because the app has no live or verified event data provider.

## Personal attendance rules

- Records are editable by the student and separated by semester and subject.
- Each entry is Present, Absent, or Cancelled; Cancelled entries do not count toward the denominator.
- Attendance percent is `present / (present + absent) × 100`.
- STME and SOC show an 80% personal target; SOL shows a 70% personal target.
- These are advisory calculations from the student's own records, not official NMIMS attendance.

## Current storage

`DatabaseHelper` schema version 7 creates `students`, `academic_periods`, `personal_subjects`, `attendance_entries`, and `assignments`. Personal periods, subjects, sessions, and assignments use `owner_account_id`. Assignment subjects are currently labels, so an assignment is not permanently linked to a semester.

## Campus destination

The map shows only NMIMS Chandigarh. Its campus pin uses the coordinates in the Google Maps embed on the [official NMIMS Chandigarh contact page](https://chandigarh.nmims.edu/stme/contact-us/). The Maps SDK key is supplied through `local.properties`; a placeholder remains as the build fallback when no key is configured.

## External account service still required

Registration and sign-in currently work against a local account store only; password reset does not work offline. None of these flows use a real account service yet. No API base URL, OpenAPI description, auth token contract, verified email/reset workflow, or sync service is present. Required inputs are tracked in [API_GAPS.md](API_GAPS.md). No client endpoint should be invented from the proposed contracts there.
