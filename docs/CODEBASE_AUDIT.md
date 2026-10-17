# CampusMate codebase audit

Audit date: 2026-10-16

## Fixes made

- **One navigation path per primary feature:** Home, Attendance, Assignments, Campus, and Profile are direct bottom tabs. Removed the duplicate Planner hub and drawer copies of primary destinations. The empty Events feed and its unused model/adapter were removed because no live or verified event provider exists.
- **One attendance source of truth:** Home reads current-semester subjects and personal attendance entries from the same repositories as Attendance. Assignments now offers subjects from the current semester instead of querying the nonexistent `subject_enrollments` table.
- **Existing installs:** Schema version 8 creates the local account table and, for older installs, missing semester, subject, and attendance tables with `IF NOT EXISTS` during upgrades. The migration does not drop those tables.
- **Clear personal-record state:** The dashboard starts with loading text, uses an honest no-semester/no-session state, and labels attendance as personal rather than official. Unknown departments are not assigned an 80% target.
- **Local registration and sign-in:** Registration creates the profile and salted PBKDF2 password verifier transactionally; sign-in verifies SAP ID, email, and password off the UI thread. The app states that the account is device-local and the email domain is not verified. Password reset remains unavailable without verified email ownership. Profile editing is disabled if no local profile exists.
- **Persistent profile photos:** The profile picker now requests a document URI and persists its read permission where the provider allows it.
- **Campus-specific map:** The address and embedded map pin were checked against the [official NMIMS Chandigarh contact page](https://chandigarh.nmims.edu/stme/contact-us/). The same coordinate constants drive map centering and directions; the campus action buttons use short, aligned labels and external-link failures are handled.
- **Logo and design:** Replaced the Android robot launcher artwork and mortarboard mark with an editable book-and-location vector. The Figma token board now matches Android colors; added eleven light-only app flow frames and the logo lockup in the [CampusMate Figma file](https://www.figma.com/design/ouZsqimZfYoZj9cCyF5PiI).
- **Dead code/docs:** Removed the unused legacy attendance repository/model/adapter and updated data-layer and demo docs to reflect the implemented local-only behavior.

## Integration limits

- No backend base URL, OpenAPI schema, auth/token contract, password-reset contract, or sync API exists in this project. Registration and sign-in work only against the local device account store. Verified NMIMS identity, password reset, attendance sync, assignment sync, and profile sync are **not implemented**. No endpoint has been invented.
- Assignment records retain a subject label, not a semester/subject ID. The dialog defaults to current-semester subjects, but existing assignments are not semester-linked.
- The map requires a valid Maps key and device runtime to verify tile loading and launch behavior. A key was present in this machine's `local.properties`; its value is intentionally not recorded here.
- SQLite instrumentation and visual/device checks require an emulator or device. A static build does not prove those runtime flows.

## Verification record

- `./gradlew lintDebug testDebugUnitTest assembleDebugAndroidTest assembleDebug --no-daemon --no-parallel --max-workers=1 --console=plain`: **BUILD SUCCESSFUL** on 2026-10-16.
- 34 local unit tests passed; instrumentation APK compiled but was not run because `adb devices -l` listed no connected devices.
- Lint had no errors and five notices: three adapter refresh efficiency notices, one Gradle wrapper update notice, and one Maps SDK update notice.
- See [FINAL_QA_REPORT.md](FINAL_QA_REPORT.md) for the verified limitations and required backend inputs.
