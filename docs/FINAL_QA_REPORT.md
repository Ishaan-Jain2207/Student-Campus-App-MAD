# CampusMate audit and verification

Audit date: 2026-10-16

## Delivered

- Simplified primary navigation to Home, Attendance, Assignments, Campus, and Profile. Resources and Sign out remain in the drawer. Removed the empty Events and duplicate Planner paths.
- Made the warm light palette and components consistent, moved visible screen copy into Android string resources, aligned attendance controls, and added accessible labels for the four Attend/Miss plus/minus actions.
- Replaced generic launcher artwork with the editable book-and-pin CampusMate mark. The [Figma file](https://www.figma.com/design/ouZsqimZfYoZj9cCyF5PiI) contains the token board, logo, and 11 light-mode app flow frames.
- Connected Home, Attendance, and Assignments to the same local semester/subject/attendance repositories. Personal attendance remains editable and semester-scoped; canceled sessions do not count. Targets are advisory: 80% for STME/SOC and 70% for SOL.
- Removed fake seeded identity/attendance content. Registration now creates a device-local profile and salted password verifier; sign-in authenticates locally. Profile image URI access is persisted where supported.
- Set the campus map to the NMIMS Chandigarh location shown by the [official campus contact page](https://chandigarh.nmims.edu/stme/contact-us/). One campus destination is used for map centering and directions.
- Updated database migration to create personal attendance and local account tables idempotently without dropping existing tables. Removed the redundant splash activity and use Android's platform launch screen.
- Corrected stale QA and test documentation so it distinguishes implemented local behavior from proposed integrations.

## Verification actually run

Command:

```text
./gradlew lintDebug testDebugUnitTest assembleDebugAndroidTest assembleDebug --no-daemon --no-parallel --max-workers=1 --console=plain
```

Observed: **BUILD SUCCESSFUL**. 34 local unit tests passed. The debug app and Android test APK assembled; instrumentation sources compiled. No emulator or device appeared in `adb devices -l`, so instrumented tests, visual layouts, map tile loading, and live device behavior were not executed.

Lint reported five non-blocking findings: three `notifyDataSetChanged` efficiency notices, one Gradle wrapper update notice, and one Google Maps SDK update notice. No lint errors remained. Dependency upgrades were not made as part of this UI/codebase cleanup.

## Integration blockers and limits

- There is no backend URL, OpenAPI schema, HTTP client, campus identity verification, password-reset service, or sync worker. Registration and sign-in now work against a device-local account, using a salted PBKDF2 password verifier. `@nmims.in` is checked for format only; NMIMS does not verify the address. Password reset and cross-device sync are unavailable.
- Attendance records are the student's private on-device record, not official NMIMS attendance. Local data does not sync across devices.
- Assignment subject selection defaults to the active semester, but assignments store the subject as text rather than a stable subject/semester ID.
- A Maps key is configured locally, but live map behavior needs a runtime/device check. Its value is deliberately not included in this report.
- The archive is not a Git repository, so this report cannot provide a Git diff or clean working-tree proof.

## Required next integration inputs

The backend owner must provide the real HTTPS base URL and approved API contract for registration, sign-in, password reset, profile retrieval, and any requested sync. Until then, no endpoint should be inferred from the examples in [API_GAPS.md](API_GAPS.md).
