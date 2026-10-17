# CampusMate (NMIMS Chandigarh) — Global Agent Rules & Workflow

**Source of Truth:** [CampusMate_Gemini_Master_Prompts.md](CampusMate_Gemini_Master_Prompts.md)

---

## Campus & Domain Context

* **Institution:** SVKM's NMIMS Chandigarh
* **Address:** Plot No. 5, Education City, Opposite Botanical Garden, adjacent to Govt. Senior Model School, Sarangpur, Chandigarh (U.T.) 160014.
* **Departments (Exactly 3):** `STME` (School of Technology Management & Engineering), `SOC` (School of Commerce), `SOL` (School of Law).
* **Attendance Warning Minimums:** STME 80%, SOC 80%, SOL 70%. Show clear warning banner when attendance falls below requirement.

---

## Global Constraints & Absolute Rules

1. **Read Master Plan First:** Always refer to `CampusMate_Gemini_Master_Prompts.md` before making architectural or code changes.
2. **Absolute Data Integrity:** Never fabricate, seed, or disguise fake/dummy data as real: no fake SAP IDs, fake attendance, fake assignments, fake events, or fake success messages. Display clean, designed empty states when data is unavailable.
3. **Strict Tech Stack:**
   - Language: Java only (No Kotlin).
   - UI: XML Layouts & Views (No Jetpack Compose, Flutter, or React Native).
   - Database: Native SQLite with `SQLiteOpenHelper`.
   - Mapping: Official Google Maps SDK for Android (`SupportMapFragment`) focused on NMIMS Chandigarh campus.
4. **Authentication & Email Domain:**
   - Registration: Full Name, SAP ID, institutional email (`@nmims.in`), Year (1–5), Department (`STME`, `SOC`, `SOL`), Password, Confirm Password.
   - Sign-in: SAP ID, `@nmims.in` email, Password.
   - Never store plain passwords in SQLite, SharedPreferences, or logs.
5. **Editable Personal Attendance:**
   - The current user requirement is a student-managed, semester-based personal log. Students can add subjects and attended/missed sessions and correct their own entries. This is not an official university feed. Do not implement or describe it as read-only.
6. **Parameterized SQL:** Always use parameterized SQL (`?`) for database queries.
7. **Campus Atlas Visual System:**
   - Canvas: Warm Paper `#F7F5EF`
   - Primary Ink: Evergreen Charcoal `#18231F`
   - Secondary Text: Muted Slate `#66716B`
   - Primary Accent: Moss `#365942`
   - Secondary Accent: Deep Persimmon `#A94327`
   - Surface: Warm White `#FFFEFA`
   - Divider/Border: Subtle Grey `#DDDCD3`
8. **Accessibility & Usability:** Minimum 48dp touch targets, clear `sp` text sizes, high contrast, meaningful content descriptions, keyboard-safe scrollable forms.
9. **Phased Execution:** Implement phase-by-phase as authorized. Never start the next phase without explicit user approval.

---

## Current User-Approved Product Overrides (2026-10-16)

These explicit user requirements supersede conflicting older wording in the master prompts above:

1. **Attendance is student-managed and editable.** It is a private, semester-based personal record, not an official university feed. Students can add subjects, log attended/missed sessions, and correct entries. Keep a clear non-official disclaimer. Do not imply NMIMS verifies its entries.
2. **Light-only appearance.** Keep the app on a light theme even when Android's system theme is dark. Use the semantic palette in `design-system/campusmate/MASTER.md` and keep Figma aligned with the Android resources.
3. **Attendance interaction.** Every subject card exposes separate Attend and Miss segmented −/+ controls. `+` adds one dated session. `−` asks before removing the latest matching record. Keep one row per class session, including multiple classes on one date.
4. **Primary navigation.** Home, Planner, Attendance, Campus, and Profile are available from the bottom navigation. Secondary sections remain in the drawer.

## Testing Setup

- Dependency injection: Hilt; Java sources use constructor/field injection.
- Local tests: JUnit 4 (`./gradlew testDebugUnitTest`).
- SQLite integration tests: AndroidJUnit4 instrumented tests (`./gradlew connectedDebugAndroidTest` with an emulator/device). They use unique test-only accounts and assert persistence and account scoping; no demo student is seeded into the app.
- Instrumented APK compile check: `./gradlew assembleDebugAndroidTest`.
- Coverage report: `./gradlew jacocoTestReport`; HTML output is under `app/build/reports/jacoco/jacocoTestReport/html/`.
- Review the test report and device availability before claiming runtime UI coverage. A successful APK build does not prove the app was launched on a device.
