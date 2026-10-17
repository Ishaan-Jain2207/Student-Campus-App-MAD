# CampusMate requirement and evidence matrix

| Area | Current implementation | Evidence / limit |
|---|---|---|
| Registration | Creates a device-local profile and account after validating name, numeric SAP ID, `@nmims.in` format, department STME/SOC/SOL, year 1–5, password and confirmation | Email ownership and NMIMS enrollment are not verified; account stays local |
| Sign in / reset | Separate SAP ID, email, and password fields; reset requests SAP ID and institutional email | Login verifies the local account; password reset remains unavailable because no verified email service exists |
| Home | Loads greeting, next assignment, current-semester personal attendance, and open assignment count from local SQLite | No official attendance feed or cross-device sync |
| Attendance | Student-editable, subject-wise, semester-scoped records with Attend/Miss plus/minus controls; cancelled sessions excluded | Advisory target is 80% for STME/SOC and 70% for SOL; record is not official NMIMS attendance |
| Assignments | Local assignment list, completion toggle, delete, due date, subject selected from current semester | Subject is stored as text and is not linked to the semester after creation |
| Navigation | Bottom tabs: Home, Attendance, Assignments, Campus, Profile. Drawer: Resources and Sign out | Events feed was removed because no verified data integration existed |
| Campus | Google Maps SDK shows one NMIMS Chandigarh destination; directions use the same campus coordinates and browser fallback | Address and embedded map location checked against the [official NMIMS Chandigarh contact page](https://chandigarh.nmims.edu/stme/contact-us/). Live map tiles still require a device/API key check |
| Profile | Local profile display and edit; persistent document URI for the selected photo | If no local profile exists, editing is disabled and no profile row is fabricated |
| Resources | Curated official NMIMS Chandigarh resource links read from the bundled CSV | Content is bundled and may become stale; it is not live-synced |
| Branding | Light-only color tokens and editable logo/screens in Figma; matching Android vector/adaptive launcher icons | Figma: [CampusMate design](https://www.figma.com/design/ouZsqimZfYoZj9cCyF5PiI) |
| SQLite | Device-local authentication plus account-scoped periods, subjects, attendance entries, and assignments; schema version 8 | Instrumentation source exists; device execution depends on an available emulator/device |

See [CODEBASE_AUDIT.md](CODEBASE_AUDIT.md) for the fixes applied and checks actually run.
