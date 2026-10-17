# CampusMate testing setup and evidence

Last verified: 2026-10-16

## Stack

| Area | Setup |
|---|---|
| App | Java 17, Android Views/XML, Material Components |
| Local tests | JUnit 4 |
| Device tests | AndroidJUnit4, Espresso, Hilt test runner |
| Coverage | JaCoCo |
| Not available here | Connected Android device/emulator; automated screenshot comparisons |

## Verification command

```bash
./gradlew lintDebug testDebugUnitTest assembleDebugAndroidTest assembleDebug --no-daemon --no-parallel --max-workers=1
```

This completed successfully on 2026-10-16. The one-worker setting avoids a transient lint race against generated Hilt test sources seen during a parallel run.

- 34 local unit tests passed.
- Android instrumentation sources and APK compiled, but were not run because `adb devices -l` returned no devices.
- Lint completed with no errors and five notices: three `notifyDataSetChanged` efficiency findings and two dependency/toolchain update notices.
- Debug and Android test APKs were assembled. This is build evidence, not proof of runtime behavior.

## Test coverage

Unit coverage includes attendance thresholds and summaries, campus coordinates, assignment validation, resource CSV parsing, login/registration validation, and repository/model contracts. Instrumentation tests cover SQLite persistence/account scoping and bottom-navigation behavior; they require a device to execute.

No test supplies a production student account or fake campus records. Test fixtures use isolated test-only data.

## Runtime checks still required

With an emulator or device, run `./gradlew connectedDebugAndroidTest`, then visually inspect Home, Attendance, Assignments, Campus, Profile, Resources, sign-in, and registration at phone and tablet sizes and with larger font settings. Verify plus/minus actions, delete confirmation, offline local persistence, accessible labels, and Google Maps behavior. Real authentication/reset/sync checks must wait for the backend contract.

Design reference: [CampusMate Figma](https://www.figma.com/design/ouZsqimZfYoZj9cCyF5PiI).
