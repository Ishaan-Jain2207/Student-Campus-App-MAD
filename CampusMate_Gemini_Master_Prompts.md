# CampusMate — Gemini Agent Master Plan & Phase-by-Phase Prompts

**Purpose:** A complete, strict implementation playbook for building CampusMate in Android Studio with Gemini Agent. This document is the project’s source of truth. Keep it in the project root and refer to it before every phase.

**Assignment deadline in the supplied brief:** 20 October 2026  
**Required implementation:** Native Android, Java, XML Views, SQLite, TXT/CSV file handling, Google Maps, working navigation and interactions.

---

## How to use this document

1. Create/open the Android Studio project named **CampusMate**.
2. Save this file in the project root as `CampusMate_Gemini_Master_Prompts.md`.
3. Give Gemini Agent **Phase 0 only**. Review its audit and proposed architecture before authorizing Phase 1.
4. For each later phase, paste that phase’s entire prompt into Gemini Agent. Do not ask it to implement multiple phases at once.
5. After each phase, inspect the diff, run the build, launch the app, and complete the listed manual checks. Commit or create a backup after every passing phase.
6. If a phase fails, use the **Repair Prompt** at the end of this document. Do not keep layering new features over a broken build.
7. Do not tell the agent “make it better” without defining the problem. Give it the failing behavior, logs, screenshot, and expected behavior.

### Required working rule

Gemini must **inspect → plan → implement the smallest safe change → build → test → report**. It must never merely claim that a feature works. A successful compile is not proof that a screen, API integration, or database flow works on a device.

---

# PART A — Product definition (source of truth)

## 1. Product concept

### Product name
**CampusMate**

### Product idea
A student’s **campus field guide and academic command center**: a calm, useful place to answer the questions students repeatedly have during a busy day:

- What should I pay attention to today?
- Which assignments are due next?
- What attendance have I recorded?
- Where is a campus location or event?
- Where can I find useful campus information?
- Where can I update my student details?

This is not a social network, a college ERP, or a generic dashboard full of meaningless statistics. Keep the scope focused on the assignment and make every shown action useful.

### First-principles design rule
Design from a student’s task, not from a collection of screens. Every screen must have one primary job, a clear primary action, useful feedback, and a safe way to recover from errors. Reduce the time and taps required to complete frequent tasks. Use realistic empty states when data does not exist; never disguise missing functionality with fake data or pretend-save messages.

## 2. Explicit design direction — DO NOT COPY THE REFERENCE IMAGE

Create a fresh visual identity called **Campus Atlas**: an editorial campus field guide mixed with a modern productivity tool. It should feel intentional, tactile, calm, and distinctive—not like the reference’s blue header/card layout and not like a default Material sample app.

### Visual language
- **Canvas:** warm paper `#F7F5EF`.
- **Primary ink:** deep evergreen-charcoal `#18231F`.
- **Secondary text:** muted slate `#66716B`.
- **Primary accent:** persimmon/coral `#E86B4A`.
- **Secondary accent:** moss `#55745C`.
- **Surface:** clean warm white `#FFFEFA`.
- **Divider/border:** subtle warm grey `#DDDCD3`.
- **Danger:** accessible dark red, used only for destructive/error states.
- Do not use a full-screen blue gradient, glowing effects, glassmorphism, or a stack of identical rounded cards.
- Use high contrast, restraint, whitespace, thin editorial rules, section labels, clear typographic hierarchy, and a limited accent palette.
- Do not use emoji as interface icons. Use vector drawables or a consistent icon library already present in the project.
- Avoid copying Apple, Linear, Notion, or the assignment reference screen-for-screen. Those are only quality-level inspiration; the Campus Atlas composition and palette must stand on their own.

### Layout behavior
- Base spacing scale: `4, 8, 12, 16, 20, 24, 32` dp. Use it consistently.
- Minimum practical tap target: 48dp for touch controls.
- Corner radii should be purposeful: small on fields/chips, moderate on important panels, not a rounded rectangle around every item.
- Use typography from Android/system-compatible fonts unless the project already includes a licensed font. Do not add huge font downloads or obscure fonts.
- Use `sp` for text sizes and `dp` for layout dimensions.
- Keep screens usable on a small emulator and with long names, long assignment titles, and large system font settings. Avoid hardcoded screen-wide assumptions.
- Use `ScrollView` or RecyclerView appropriately; no content should be cut off behind navigation or the system bars.
- Support portrait first. Do not break layout when the keyboard opens.
- Provide visible pressed, selected, disabled, error, and loading states when relevant.
- Provide empty states with a plain-language explanation and a next action.

### Signature composition
The dashboard should feel like a **daily briefing**, not a statistics template:
1. A compact greeting/date area with a strong editorial heading.
2. One useful “Today / Next up” area that changes based on stored data (e.g., nearest incomplete assignment or an empty-state action).
3. Two compact academic summaries: attendance and open assignments, derived from SQLite.
4. A clear quick-action area for Mark Attendance and Add Assignment.
5. A small upcoming-events preview, if events are available.
6. No fabricated GPA, percentage, alerts, due dates, notifications, or event counts.

### Screen-specific design intent
- **Splash:** brief, branded, quiet; no long fake loading delay. Respect Android splash-screen conventions and move forward reliably.
- **Login:** welcoming and focused, with clear labels, keyboard types, show/hide password control if practical, validation beside the relevant field, and a clear explanation that this assignment uses local demo access rather than a real authentication backend.
- **Dashboard:** daily briefing and actionable information; no meaningless decorative charts.
- **Attendance:** quick subject/date/status capture with strong selected-state affordance; easy-to-scan records list and visible success/failure feedback.
- **Assignments:** a task list organized by due date/status, clear overdue/today/upcoming states where calculable, and an obvious Add action. Editing/completion must update persistence.
- **Events:** image-led but restrained event list; details open with name/date/venue and any available information. Use local bundled demonstration events when no service is part of the assignment, and label seeded examples as sample content if appropriate.
- **Campus Map:** map is the visual focus, with clearly identifiable markers and a useful list/legend of locations. The screen must remain understandable if Google Maps fails to load or API setup is incomplete.
- **Profile:** student details are easy to read and edit. Use a bundled placeholder avatar or initials when no photo exists. Do not request broad storage access merely to pick a profile image.
- **Resources/file handling:** show content read from a real bundled `.txt` or `.csv` asset. Do not hardcode the displayed content and call it file handling.
- **Menu/navigation:** unmistakable selected state, useful labels, predictable back behavior, and logout confirmation.

## 3. Required screen and navigation model

Use this proposed information architecture unless Phase 0 finds a concrete incompatibility in the project:

- **Splash → Login → Main application shell**
- Main shell: one `MainActivity` hosting native Java Fragments, with a consistent toolbar and app navigation.
- Four bottom destinations may be used for high-frequency sections: **Home**, **Planner**, **Campus**, **Profile**.
- The navigation drawer/menu must expose the assignment’s individual destinations: **Home, Attendance, Assignments, Events, Map, Profile, Logout**. Resources may be added as an additional destination.
- **Planner** is a small hub or grouped section that links to the standalone Attendance and Assignments screens; it must not replace them.
- **Campus** may provide clear routes to Events, Campus Map, and Resources. Events and Map still need their own usable screens, accessible from the drawer/menu.
- Do not create multiple competing navigation systems. The bottom navigation is for primary sections; the drawer exposes all required destinations. Keep the selected state synchronized whichever route opens a screen.
- Prevent duplicate fragments/screens from piling up when a user taps the current destination repeatedly.
- The back button should return to the appropriate previous/root destination without unexpected loops. From the main root, it may ask for exit confirmation or follow normal Android behavior. From Logout, clear the local session and return to Login.

### Implementation style
- Use Java classes and XML layouts only.
- Recommended baseline: `MainActivity` + Fragments, `LoginActivity` if needed, model classes, repositories/database helper, RecyclerView adapters, and a small set of utility classes.
- Use ViewBinding only if the existing project/build setup supports it cleanly; do not rewrite Gradle tooling just to add it.
- AndroidX/AppCompat/Material Components/Fragment/RecyclerView/Google Maps SDK are acceptable if appropriate for the project. Inspect current Gradle and dependency versions first. Do not blindly upgrade Android Gradle Plugin, Gradle wrapper, compile SDK, or every dependency.
- SQLite must use `SQLiteOpenHelper` or an equally explicit SQLite implementation. Do not substitute Firebase, Room, an API, or SharedPreferences for required SQLite records.
- Keep the app offline-first except for the actual Google Maps tile/service integration.

## 4. Functional requirements from the assignment

These requirements are mandatory. Do not silently replace them with mockups.

### Screen requirements
1. Splash screen with app name/logo.
2. Login screen with username/email, password, and Login button.
3. Home/dashboard with welcome message and access to Attendance, Assignments, Events, and Campus Map.
4. Attendance screen: subject selection, date selection, Present/Absent radio buttons, Save attendance, and visible attendance records.
5. Assignments screen: assignment name/title, subject, due date, Completed checkbox, Save action, and saved assignments displayed when revisiting the screen.
6. Events screen: event name, date, venue, event image, and View Details action/dialog.
7. Campus Map with Google Maps and at least three campus location markers.
8. Profile screen with student photo, name, roll number, department, and semester.
9. Menu/navigation with Home, Attendance, Assignments, Events, Map, Profile, and Logout.

### Required Android UI/API controls
- `TextView`
- `EditText`
- `Button`
- `ImageView`
- `CheckBox`
- `RadioButton` (use a `RadioGroup` for exclusive Present/Absent choice)
- `Spinner` for subject selection
- Android `DatePicker` / `DatePickerDialog`
- `Toast` for short feedback where appropriate
- `AlertDialog` for confirmation/details where appropriate
- Menu/navigation

Do not merely declare these controls somewhere. Use them in the intended user flows and verify the behavior.

### Data and file requirements
- SQLite stores student information, attendance, and assignments.
- A saved assignment remains visible when the user leaves/reopens the screen and after app relaunch.
- Attendance records persist and can be read back.
- Include a real asset in `app/src/main/assets/` such as `campus_resources.txt` or `campus_resources.csv`; read it using Java I/O and display the parsed/read contents in the app.
- Google Maps SDK must display an interactive map with at least three named location markers. A static image is not an implementation.

### Interaction path examples to verify
- Login → Home.
- Home → Attendance → choose subject/date/status → Save → record visible.
- Home → Assignments → enter details → Save → assignment visible.
- Home/Campus → Map → map and at least three named markers visible.
- Events → View Details → details shown in a dialog or detail view.
- Profile → edit/save details → updated details are visible after leaving and returning.
- Menu → every required destination; Logout → Login.

## 5. Data model and behavior rules

Phase 0 must inspect the project and refine table details, but retain this minimum logical schema.

### Student
At minimum: stable local ID, name, roll number, department, semester, optional photo URI/string. Seed one clearly identified demo student if needed so the profile works on first launch. Let the user edit these fields. The seed must not be mistaken for a real authenticated student.

### Attendance
At minimum: ID, subject, attendance date in ISO format (`yyyy-MM-dd`), status (`PRESENT` or `ABSENT`), created timestamp. Prevent accidental duplicates for the same subject/date or handle an existing row as an edit/update. Define that behavior and show a clear message.

### Assignment
At minimum: ID, title/name, subject, due date in ISO format, completed integer/boolean, created timestamp, updated timestamp if edit/update is supported. Include the ability to mark an item complete and persist it. Validate required fields.

### Database rules
- Use parameterized SQL (`?`) for user-provided values.
- Close cursors and other resources correctly.
- Use transactions when an operation involves multiple related writes.
- Increment `SQLiteOpenHelper` database version and write a migration for schema changes; do not delete user data just to fix a schema.
- Use deterministic ordering: due date ascending for incomplete assignments, date descending for attendance history unless UX suggests otherwise.
- Re-query/reload records after inserts/updates/deletes so UI reflects database truth.
- Distinguish “no records” from “database error”.
- Do not report success if insert/update returns failure.
- Derive dashboard counts and percentages from persisted data. Define the denominator for any attendance percentage clearly; if the app only stores per-subject dates and no timetable, label it as **recorded attendance**, not official institutional attendance.

### Local demo login rules
The assignment does not require a backend authentication service. Implement only a clearly described **local demo login** unless an existing project already supplies a real, approved auth mechanism.
- Validate email/username and non-empty password appropriately.
- Never claim that it is secure server authentication.
- Never store a plaintext password in SQLite or SharedPreferences.
- Save only a minimal non-sensitive local session flag/identifier in SharedPreferences if needed.
- On logout, clear the local session and navigate to Login.
- Do not invent registration, verification, password reset, or backend features outside the scope unless explicitly approved.

### Campus map rules
- Use the official Google Maps SDK for Android, not a WebView screenshot or static map.
- Put marker definitions in one repository/config class rather than scattering coordinates across UI code.
- Include at least three named marker entries, for example Library, Block A, Cafeteria, only after their coordinates are supplied or verified.
- Do **not** fabricate coordinates and present them as real. If the campus location/coordinates are unknown, create clearly labelled configuration placeholders and ask for confirmation before claiming real campus accuracy. Document exactly where to update the coordinates.
- Keep the app buildable when the API key is not supplied, if technically possible; show a clear setup state rather than crashing. Map verification still requires a valid API key and device/emulator network access.
- Never commit a real API key to source control. Read it from local project configuration (`local.properties` or an existing secure configuration path), add the right manifest placeholder for the project’s Gradle DSL, and document setup. Do not change Gradle DSL blindly.

### File handling rules
- Bundle a real `.txt` or `.csv` file under `app/src/main/assets/`.
- Read it at runtime using a clear Java method and display the actual read data.
- Handle missing asset, empty file, malformed CSV rows, and I/O exceptions gracefully.
- Do not request external-storage permissions for a bundled asset.
- Keep CSV parsing simple but correct: handle blank lines and missing columns; if data can contain quoted commas, use a real CSV parser only if already available or implement a safe small parser with tests. Do not casually split every line on commas if quoted values are possible.

## 6. Acceptance philosophy

A requirement is **Done** only when all are true:
1. The code exists in the right place.
2. The project builds successfully.
3. The screen/action can be exercised in the app.
4. The expected result is visible.
5. Persistence/network/configuration behavior has been checked where applicable.
6. Error and empty states are not crashes.
7. The agent records evidence and any remaining manual test requirement.

Use three statuses: `PASS`, `FAIL`, and `MANUAL VERIFICATION REQUIRED`. Never mark an untested Google Maps API-key flow as `PASS` just because the Java code compiles.

---

# PART B — Global agent rules

Paste these rules into `AGENTS.md` in Phase 0 and keep the file in the project root.

1. **Read this master plan before changing code.** Treat it as the source of truth for design, scope, architecture, and acceptance criteria.
2. **Respect the stack:** Java + XML Views + SQLite + actual Google Maps SDK. No Kotlin, Compose, Flutter, React Native, Firebase, or backend substitution.
3. Inspect the current repository, Gradle files, manifest, packages, resources, and existing tests before editing. Do not assume a blank project.
4. Do not replace working code merely because a different structure is preferred. Explain why a change is necessary and keep diffs focused.
5. Do not bulk-regenerate the entire app, XML, Gradle files, or resources unless there is a demonstrated reason.
6. Before changing a file, understand its callers, IDs, navigation paths, and associated resources. Search references before renaming classes, methods, resource names, or view IDs.
7. Do not leave TODOs, fake buttons, placeholder click listeners, empty methods, “coming soon” screens, or success Toasts that are not backed by successful operations in required user flows.
8. Sample content is allowed for events/resources where the assignment has no live feed, but label sample data where necessary. User-entered attendance, assignments, and profile data must use the specified persistence.
9. Do not fabricate institution-specific coordinates, current events, security, or successful API operations. State assumptions explicitly.
10. Validate all user inputs, handle errors, and show messages that tell users how to recover.
11. Keep UI state and SQLite state synchronized. Reopen screens and relaunch the app during persistence tests.
12. Follow accessibility basics: meaningful content descriptions, accessible contrast, readable `sp` sizes, sensible focus order, and approximately 48dp minimum touch targets.
13. Use Android lifecycle correctly. Avoid retaining Activity references in long-lived objects, leaked dialogs, blocking the main thread with unnecessary I/O, or touching views after a Fragment’s view is destroyed.
14. Use parameterized database queries and never store passwords in plaintext.
15. Avoid unnecessary dependencies, permission requests, background services, network calls, and Gradle upgrades.
16. Every implementation phase must end with: files changed, requirements covered, build/test results, manual verification steps, known issues, and next phase readiness.
17. Run the actual build command appropriate to the project (`./gradlew assembleDebug`, or the project’s wrapper equivalent). Do not report a build passing from static inspection alone.
18. If the build or tests fail, fix the cause and rerun relevant checks. Do not hide failures, weaken tests, or remove required features to get a green build.
19. If Android Studio/Gradle cannot run in the agent environment, say so precisely. Still run all available static checks and give exact commands the user must run locally.
20. Stop at the end of the requested phase. Do not begin the next phase without authorization.

---

# PART C — Phase prompts

> **Important:** Copy only the phase prompt you are authorizing. Keep the global rules above and `AGENTS.md` active throughout the project.

## PHASE 0 — Repository audit, requirement map, architecture and implementation plan

### Goal
Understand the existing project and remove ambiguity before creating implementation code. **Do not build the application features in this phase.**

### Copy-paste prompt

```text
You are starting Phase 0 of CampusMate. Read `CampusMate_Gemini_Master_Prompts.md` completely, especially Product Definition, Global Agent Rules, and the assignment requirements. Treat it as the source of truth. If this file is not available in the workspace, stop and ask me to put it in the project root; do not guess its contents.

TASK: AUDIT AND PLAN ONLY. Do not implement the application screens/features yet.

1. Inspect the complete project tree, Android manifest, Gradle wrapper, root/app Gradle files, Java packages, XML layouts, resources, launcher configuration, existing tests, and current build status. Identify whether the project is new or already contains code. Preserve useful work.
2. Record exact toolchain values already configured: Gradle version, Android Gradle Plugin, compileSdk, minSdk, targetSdk, Java source/target compatibility, applicationId, namespace, and existing dependencies. Do not upgrade them during the audit.
3. Build a requirement traceability table with one row for every assignment requirement/control and columns: requirement, planned screen/file, implementation strategy, verification method, status. Use PASS/FAIL/NOT IMPLEMENTED/MANUAL VERIFICATION REQUIRED honestly.
4. Propose the project architecture based on this master plan. Default to a single MainActivity + Java Fragments for the signed-in app, a login flow, XML Views, repository/database helper layer, model classes, and RecyclerView adapters where useful. If the existing project makes this unsafe, explain the precise evidence and propose the smallest alternative.
5. Create an exact screen map and user-flow map. The navigation must expose Home, Attendance, Assignments, Events, Map, Profile, and Logout; Resources must be reachable if implemented. Avoid duplicate navigation systems and loops.
6. Propose package names and a file tree. List every planned Activity, Fragment, adapter, model, repository, database helper, XML layout, drawable, values resource, asset, and test. Keep the initial scope small enough for this assignment.
7. Propose SQLite schema and behavior: Student, Attendance, Assignment. Specify columns, data types, constraints, date format, sort order, insert/update/duplicate behavior, database version/migration approach, and how screen data will refresh after writes. Never store passwords.
8. Identify unknowns that require my decision, especially confirmed campus marker coordinates and Google Maps API-key setup. Do not invent a real campus location. Make reasonable reversible defaults for noncritical decisions and list them explicitly.
9. Specify the Campus Atlas visual system: colors (#F7F5EF, #18231F, #66716B, #E86B4A, #55745C, #FFFEFA, #DDDCD3), spacing scale, typography hierarchy, field/button/list states, reusable components, empty/error/loading states, and accessibility requirements. Clearly state how the design differs from the assignment reference.
10. Plan phases 1–9 with dependencies, scope boundaries, exit criteria, risks, and build/test checks.
11. Create `AGENTS.md` in the project root containing the concise global constraints/workflow from this master plan and a reference to `CampusMate_Gemini_Master_Prompts.md`. Do not overwrite existing project instructions without merging and explaining the change.
12. Create `docs/REQUIREMENTS_TRACEABILITY.md` with the requirement matrix and `docs/IMPLEMENTATION_PLAN.md` with the architecture, file map, database proposal, navigation map, design tokens, open questions, and phase exit criteria. Create only planning documents, not feature code.
13. Run the current build only to establish a baseline if practical. Do not modify code to make a broken baseline pass during this phase.

OUTPUT: Summarize findings, list created/changed files, report baseline build output, show the important architectural decisions and unresolved items, and ask for approval to start Phase 1. Stop after this report. Do not implement Phase 1.
```

### Exit criteria
- [ ] Agent inspected the real project and toolchain instead of assuming them.
- [ ] Requirement traceability table exists.
- [ ] Screen/navigation map and planned file structure exist.
- [ ] SQLite schema is proposed with sensible uniqueness/update behavior.
- [ ] Campus coordinates and Maps key are identified as decisions/setup, not fabricated.
- [ ] `AGENTS.md`, `docs/REQUIREMENTS_TRACEABILITY.md`, and `docs/IMPLEMENTATION_PLAN.md` exist.
- [ ] Baseline build status is honestly recorded.
- [ ] You reviewed the plan and explicitly authorized Phase 1.

---

## PHASE 1 — Design system, app foundation, splash/login and navigation shell

### Goal
Establish the visual identity and technical foundation first. Deliver a stable app shell with splash, local demo login, and a navigable but clearly incomplete main shell. Do not build all feature logic yet.

### Copy-paste prompt

```text
You are implementing Phase 1 of CampusMate. Read `CampusMate_Gemini_Master_Prompts.md`, `AGENTS.md`, `docs/REQUIREMENTS_TRACEABILITY.md`, and `docs/IMPLEMENTATION_PLAN.md` before editing. Follow the Campus Atlas visual direction; do not reproduce the assignment reference design.

SCOPE: project foundation, reusable visual system, splash, login, and main navigation shell only. Do not implement SQLite attendance/assignment operations or Maps logic in this phase.

1. Re-check the current branch/project state and current build. Inspect existing resource names and dependencies before editing. Respect the current Gradle DSL and toolchain.
2. Implement the smallest required project foundation in Java and XML Views. Use compatible AndroidX/AppCompat/Material/Fragment components where necessary. Do not add libraries without a reason. Do not upgrade Gradle/AGP/SDK versions just for style.
3. Centralize design tokens in `colors.xml`, `dimens.xml`, `strings.xml`, themes/styles, and reusable drawables. Implement the agreed warm-paper, ink, persimmon and moss palette. Ensure primary text/button combinations are accessible. Keep the design systematic, not a set of screen-specific overrides.
4. Make a coherent reusable field, button, chip, divider, toolbar, list-row and empty-state style where useful. Do not wrap everything in a card. Use consistent margins and typography.
5. Implement a brief branded splash/launch transition. Do not insert an artificial long loading delay. Ensure first launch and returning-session routing are deterministic. Follow the project’s existing Android version/splash compatibility instead of introducing fragile custom timing.
6. Implement a login screen with properly labelled email/username and password fields, correct keyboard input types, password visibility control if practical, field-level validation, and disabled/loading state only where meaningful. Keep it keyboard-safe and scrollable on small screens.
7. Implement the explicitly labelled local demo login flow described in the spec. Do not store password text. Persist only a minimal session flag/identifier if needed. The flow must not claim real account authentication. Login with valid demo input should open the main shell; invalid input should show useful feedback without crashing.
8. Create the main application shell. Implement one consistent toolbar and the agreed four primary bottom destinations (Home, Planner, Campus, Profile), plus a navigation drawer/menu exposing Home, Attendance, Assignments, Events, Map, Profile, Logout (and Resources if included). Do not create competing navigation stacks. Ensure all listed destinations have temporary but intentionally styled placeholder screens until their own phase. Clearly mark placeholders in the progress report; do not call them complete.
9. Maintain correct destination selection across bottom navigation and drawer navigation. Prevent duplicate navigation when tapping the current destination. Ensure back navigation does not create loops. Logout must ask for confirmation, clear the local session, and return to Login.
10. Use Java + XML only. Do not introduce Kotlin files, Compose, Flutter, Firebase, or a backend.
11. Add basic UI tests or unit tests for any logic that is straightforward to test (such as login field validation/session helper) if the test setup supports them. Do not add a brittle test suite just to satisfy a checkbox.
12. Run `./gradlew assembleDebug` (or the exact project wrapper command). If it fails, read the first meaningful compiler/configuration error, fix the cause, and rerun. Check for XML inflation errors and incorrect resource references.
13. Inspect the changes as a diff. Do not overwrite unrelated files or leave duplicate theme definitions, dead resources, empty click listeners, or broad permission requests.

PHASE ACCEPTANCE TESTS:
- App builds from the project wrapper.
- Cold start reaches splash and then Login or the correct existing-session route.
- Login labels and validation work; password is not stored.
- Successful local demo login opens the shell; logout returns to Login.
- Every required menu destination can open a non-crashing temporary screen.
- Bottom navigation and drawer selection remain in sync.
- Keyboard does not hide essential login actions; smaller screens do not cut off content.
- Visual system visibly differs from the reference: warm paper/ink/persimmon, editorial hierarchy, restrained surfaces, and a campus-atlas identity.

REPORT: files changed; build/test commands and actual output; manual emulator checks performed/not performed; temporary screens; known defects; phase status. Stop before Phase 2.
```

### Exit criteria
- [ ] Build passes.
- [ ] Splash/login/shell work on emulator.
- [ ] Logout works and no password is persisted.
- [ ] Navigation is stable and all required routes exist.
- [ ] Core design tokens/styles are centralized.
- [ ] Placeholders are explicitly reported as incomplete.

---

## PHASE 2 — Implement all screen layouts, shared components and responsive UI

### Goal
Build the actual XML/Java screen structure for Attendance, Assignments, Events, Map, Profile, Resources and the dashboard. Focus on hierarchy, clear tasks, empty/error/loading states, and predictable navigation. Data can be wired to temporary view state only where necessary until later phases; do not invent final functionality.

### Copy-paste prompt

```text
You are implementing Phase 2 of CampusMate. Read the master plan and all current project instruction/specification files first. Inspect Phase 1 code and preserve its working navigation/session flow.

SCOPE: implement complete screen layouts and UI states for Home/Dashboard, Attendance, Assignments, Events, Campus Map, Profile, Resources/File content, and Planner. Keep Java + XML Views. Do not implement the full database or Google Maps API integration yet; do not pretend placeholder data is persisted.

DESIGN REQUIREMENTS:
- Follow Campus Atlas: warm paper canvas (#F7F5EF), ink (#18231F), muted slate (#66716B), persimmon (#E86B4A), moss (#55745C), warm-white surface (#FFFEFA), warm divider (#DDDCD3).
- Compose original screen layouts. Do not copy the PDF image or the previous generic blue-card dashboard.
- Design for student tasks: a focused daily briefing, fast academic capture, clear status, and location discovery.
- Use reusable styles/drawables/components; do not duplicate styling rules across every XML file.
- Use `sp` for text and `dp` for layout; touch targets roughly 48dp or larger; label controls; provide meaningful content descriptions; use scrollable containers/RecyclerViews where appropriate.
- Check small-width and keyboard-open behavior. No clipped content, overlapping buttons, or forms hidden by navigation/system bars.

SCREEN WORK:
1. HOME/DASHBOARD: create an editorial daily briefing with date/greeting, a useful Next Up section, attendance and open-assignment summaries, prominent Mark Attendance/Add Assignment actions, and upcoming events preview. Until data is wired in a later phase, use explicit preview/empty states; do not invent personal records or claim static values are live.
2. ATTENDANCE: subject Spinner, date selection control backed by Android DatePicker/DatePickerDialog, exclusive Present/Absent RadioButtons within RadioGroup, Save action, and an attendance history area with empty/loading/error states.
3. ASSIGNMENTS: list sorted visually by due date/status, add/edit form with title, subject Spinner or clear subject field consistent with the database proposal, DatePicker/DatePickerDialog, Completed CheckBox, save/cancel actions, and empty state. Include complete/incomplete and overdue/today/upcoming presentation based on dates; do not rely on color alone to convey status.
4. EVENTS: list of image-led event rows/cards, event name/date/venue and View Details button; details must have a designed dialog or detail layout. Use provided/bundled sample assets only; avoid broken remote image URLs.
5. CAMPUS MAP: build the screen structure and a map container/fragment host, location labels/legend, and clear marker selection states. Do not put a fake map image in place of Google Maps. If map integration is not ready, show an explicit temporary map-setup state and ensure layout can host the real SDK later.
6. PROFILE: photo/initials placeholder, name, roll number, department, semester, Edit/Save/Cancel UI, and clear validation/error state. Do not request all-files storage permission for choosing a picture.
7. RESOURCES: polished list/detail presentation that will display content read from the actual bundled TXT/CSV file in Phase 6; do not hardcode content and call it file reading.
8. PLANNER/CAMPUS HUBS: ensure they serve as useful groupings/shortcuts, not dead-end duplicate screens. Every individual required screen remains reachable from the menu.
9. NAVIGATION: fix any gaps found in Phase 1. Keep toolbar titles/back affordances and selected destination state correct; ensure every button has either real behavior or a clearly reported not-yet-wired state only during this phase.

IMPLEMENTATION QUALITY:
- Use RecyclerView for variable-length lists if the project has compatible dependencies; otherwise explain the smallest appropriate implementation. Avoid nested scrolling lists without a reason.
- Keep view references and listeners lifecycle-safe. If using Fragments, clear view binding/references in `onDestroyView()` where applicable.
- Create/choose bundled placeholder avatars/event images with correct resource references; do not use unverified image URLs.
- Do not create unnecessary permissions or external network requirements.
- Do not suppress lint warnings globally to hide a real issue.

TESTS:
- Run the Gradle debug build.
- Launch on the emulator and visit every required screen.
- Test long assignment/event names, blank/empty states, keyboard-open forms, and back navigation.
- Check for resource inflation errors, wrong IDs, missing drawables, wrong navigation mappings, and inaccessible controls.
- Report which buttons are wired and which are intentionally awaiting the correct later phase. No required action may be falsely described as complete.

Stop at the end of Phase 2. Provide changed files, build output, manual test results, remaining wiring, and Phase 3 readiness.
```

### Exit criteria
- [ ] Every required screen exists and is reachable.
- [ ] Required controls are visibly present on the correct screens.
- [ ] No clipping or obvious navigation/layout issues on a small emulator.
- [ ] Empty/error/placeholder states are designed and not misrepresented as real data.
- [ ] Build passes.

---

## PHASE 3 — SQLite data layer, models, repositories and persistence tests

### Goal
Create reliable persistence before wiring individual screens to it. Establish a clear data layer with safe SQL, migrations, and deterministic results.

### Copy-paste prompt

```text
You are implementing Phase 3 of CampusMate. Read the full master plan and project instructions. Inspect current package structure, UI models, existing database code (if any), Gradle setup, and tests before editing.

SCOPE: implement the SQLite persistence foundation for Student, Attendance and Assignment, including models/repositories/database helper and tests where possible. Do not re-design the screens in this phase. Do not replace SQLite with Room, Firebase, JSON-only persistence, or SharedPreferences for the required records.

1. Design a SQLiteOpenHelper-based database layer consistent with the architecture from Phase 0. Keep schema SQL and CRUD logic out of Activities/Fragments. Use small, testable repository/helper classes.
2. Implement Student, Attendance and Assignment model classes with explicit fields, types, validation boundaries, and predictable date formatting/parsing. Use ISO dates (`yyyy-MM-dd`) for stored date values; present them in a localized friendly format in the UI.
3. Implement tables/constraints according to the approved schema. Attendance must have deterministic duplicate handling for subject + date (e.g., a unique constraint and explicit update/upsert behavior). Assignment records must have stable IDs and a persisted completion state. Student details must be editable and persistent. Explain any deviation from Phase 0 schema before implementing it.
4. Implement create/read/update/delete operations required by the app. At minimum: get/update student; create/read attendance and handle same-subject/same-date updates; create/read/update completion/delete assignment if deletion/editing is in the UI. Never leave UI functionality with no corresponding repository method.
5. Use parameterized SQL for all user values. Use transactions when multiple writes must succeed together. Check insert/update return values and propagate clear success/failure results. Correctly close Cursor/database resources; avoid leaked cursors and swallowing exceptions.
6. Implement database versioning and migration code. Never drop user tables/data on upgrade simply to get a clean schema. If the project is still pre-release, a simple version-1 schema is fine, but migration structure must be correct for future additive changes.
7. Seed only necessary first-run demo profile data, clearly documented as sample data. Do not seed fake attendance/assignments that will look like the student’s genuine records. The database must open into valid empty states for academic records.
8. Define repository behavior for empty lists, SQL errors, duplicate attendance, invalid input, and missing student records. Do not turn all errors into an empty list with no diagnostic signal.
9. Keep database I/O appropriately off the main thread if operations or workload justify it. Do not introduce an overengineered async framework. Use a consistent callback/executor approach appropriate for the app and keep UI updates on the main thread.
10. Add unit/instrumentation tests where practical for CRUD, ordering, duplicate attendance/update, completion persistence, student profile persistence, and database upgrade behavior. Use Android’s test infrastructure already present or state why a test is manual. Do not fake database tests with mocks only when persistence is the behavior under test.
11. Provide a simple data-layer usage note in `docs/DATA_LAYER.md`: schema, constraints, methods, dates, sample data policy, migrations and known limitations.
12. Run build and available tests. Inspect SQL and resource/threading changes. Fix errors and re-run tests.

ACCEPTANCE TESTS:
- Student profile inserts/updates and remains after closing/reopening the database/app.
- Attendance insert/read works; second record for the same subject/date follows documented deterministic behavior, not a silent duplicate.
- Assignment insert/read/update-completion/delete (where included) works.
- Inputs use parameterized SQL; no password column/storage exists.
- Database version and upgrade behavior are documented and do not destructively reset data.
- Actual build and tests are reported honestly.

Stop before wiring the attendance UI. Report changed files, schema, test results, unresolved failures, and Phase 4 readiness.
```

### Exit criteria
- [ ] Tables, models, and persistence/repository APIs exist.
- [ ] CRUD and duplicate rules are tested or manually proven.
- [ ] No password storage and no destructive migration.
- [ ] Persistence survives app relaunch.
- [ ] Build and applicable tests pass.

---

## PHASE 4 — Finish Attendance end to end

### Goal
Make attendance a real, tested workflow from input to SQLite to visible history and dashboard data source.

### Copy-paste prompt

```text
You are implementing Phase 4 of CampusMate. Read the master plan, AGENTS.md, requirement matrix, Phase 3 data-layer notes, and current UI/database code first. Preserve existing working code.

SCOPE: fully connect the Attendance screen to SQLite. Do not implement unrelated features or change the database schema without explaining the reason and adding a migration when necessary.

REQUIRED FLOW:
1. The subject Spinner must show a deliberate set of subjects and a real placeholder such as “Select subject”; it must not silently default to the first subject. Keep subject options in one maintainable source. If course-specific subjects are unknown, use clearly editable demo subject values and document where to change them.
2. Date selection must use Android DatePicker/DatePickerDialog. Display the chosen date clearly and store it in the approved ISO format.
3. Present and Absent RadioButtons must be inside one RadioGroup so only one state is selected. Require a deliberate valid status selection or define and document a safe default. Do not infer status silently.
4. Save must validate subject, date and status before calling the repository. Disable/restrict invalid submission as appropriate, focus the missing field when possible, and explain what is missing.
5. Only show success Toast/inline feedback after the SQLite write returns success. On failure, show a useful message and keep the form state so the user can retry.
6. After save/update, refresh the history from SQLite and show subject, formatted date and status. Use explicit empty and error states. Sort history deterministically (newest first unless Phase 0 documented otherwise).
7. Implement duplicate subject/date behavior according to Phase 3 schema (e.g., update the existing record). Tell the user when an existing entry is updated, instead of creating accidental duplicates.
8. Support normal screen re-entry and app relaunch: the history must reload from the database, not just a temporary in-memory list.
9. Add the correct loading/empty/error behavior without long blocking operations on the UI thread. Keep Fragment view lifecycle safe.
10. Where edit/delete actions are available in the UI, implement them fully or remove the misleading affordance; do not leave a dead icon/button.
11. Make the screen match Campus Atlas design tokens. Keep the form fast to use and visually calm. Do not re-skin unrelated screens.
12. Add/update automated tests where practical for validation and repository integration. Then run the build and tests.

ACCEPTANCE TESTS:
- Empty form cannot create an invalid row.
- Spinner placeholder is not mistaken for a selected subject.
- Present and Absent are mutually exclusive.
- Date picker writes the correct date.
- Save inserts/updates SQLite and history refreshes from a database query.
- A saved row remains after leaving/re-entering the screen and after app relaunch.
- Duplicate subject/date handling matches the documented rule.
- Failure does not show a false success message or wipe the form.

Stop after Attendance is complete. Report actual tests and remaining manual checks. Do not begin assignments.
```

### Exit criteria
- [ ] Attendance is persisted, displayed, validated and survives relaunch.
- [ ] Spinner, DatePicker and RadioButtons behave correctly.
- [ ] Duplicate behavior is deterministic.
- [ ] No false success Toasts or dead controls.
- [ ] Build/tests pass.

---

## PHASE 5 — Finish Assignments end to end

### Goal
Create a practical assignment/task workflow with persistent records, meaningful sorting, reliable completion toggling, and useful feedback.

### Copy-paste prompt

```text
You are implementing Phase 5 of CampusMate. Read the master plan, project instructions, requirement matrix, and current database/repository and assignment UI code first.

SCOPE: fully implement the Assignments feature using the existing SQLite data layer. Preserve stable IDs and navigation. Do not rewrite the data layer or add unrelated features.

REQUIRED FLOW:
1. Provide a list of saved assignments loaded from SQLite, with clear empty/loading/error states. Use a RecyclerView adapter for variable-length data if available and suitable.
2. Implement Add Assignment form with title/name, subject, due date, Completed CheckBox, Save and Cancel. Use native Spinner/DatePicker controls as planned. A subject may be selected from a shared maintainable list; do not let a blank placeholder pass validation.
3. Validate title and subject as non-empty after trimming. Validate due date according to the approved UX (allow past dates if the user needs to record old work, but label it appropriately; do not silently reject legitimate historical assignments).
4. Save to SQLite using the repository and report success only after confirmed write success. On success, dismiss/close the form if appropriate, refresh from SQLite and show a brief confirmation. On failure, preserve the entered values and show actionable feedback.
5. Ensure completion state is persisted when the CheckBox is toggled. Avoid updating the wrong item due to RecyclerView position reuse; bind by stable record ID and re-query/update correctly.
6. Present useful temporal labels such as Overdue, Due today, Upcoming and Completed from the actual stored due date/completion state. Do not rely on color alone. Define how completed overdue assignments are presented.
7. Sort incomplete assignments by due date ascending, with completed items grouped/separated or sorted according to the approved design. The ordering must be stable and documented.
8. Support editing/deleting only if the UI exposes those actions. If included, implement confirmation for destructive deletion. Do not leave dead overflow menus or icon buttons.
9. Reload data whenever the user re-enters the screen and after create/update; verify after full app relaunch.
10. Update the dashboard data source only if it already has a clean contract; otherwise leave a documented integration point for Phase 8. Never hardcode the count as a final value.
11. Match Campus Atlas styles and test long titles, no assignments, multiple assignments on the same date, completion toggles, and small screens.
12. Add/update tests where practical, run build/tests, and resolve regressions.

ACCEPTANCE TESTS:
- Valid assignments save and appear in the list.
- Empty title or missing subject is rejected with useful feedback.
- Completed checkbox is persisted, including after app relaunch.
- Due-date status and sorting match the documented rules.
- Editing/deleting works if exposed; otherwise no misleading controls remain.
- Failed write produces no success message.
- Build and tests pass.

Stop after assignments are complete. Report evidence, failures and Phase 6 readiness.
```

### Exit criteria
- [ ] Assignment create/read/completion persistence works.
- [ ] Required Checkbox/Spinner/DatePicker behavior works.
- [ ] Ordering and date-state behavior is correct.
- [ ] Reopening/relaunching retains data.
- [ ] Build/tests pass.

---

## PHASE 6 — Events, Profile editing and real TXT/CSV file handling

### Goal
Complete the remaining content-focused features without unnecessary backends: useful sample events, editable/persistent profile, and actual bundled-file reading.

### Copy-paste prompt

```text
You are implementing Phase 6 of CampusMate. Read the master plan, AGENTS.md, screen layouts, SQLite layer, and test setup before editing. Preserve all working attendance/assignment flows.

SCOPE: complete Events, Profile and Resources/file handling. Do not implement Google Maps SDK integration in this phase; that is Phase 7.

A. EVENTS
1. Populate the Events screen from a small maintainable local data source (bundled asset or Java model/repository). A live events backend is not required by the assignment.
2. Each event must show a name, date, venue and image, and have a functioning View Details button that opens a useful AlertDialog or detail screen showing the event details.
3. Use bundled local image resources so event images do not randomly fail because of network URLs. Include meaningful content descriptions.
4. Clearly identify sample/demo events if needed. Do not invent claims such as official upcoming university events. Avoid hardcoded dates that become misleading; use clearly labeled sample dates or date-safe examples and document the sample content.
5. Empty event list and missing image states must be graceful. Every visible control must work.

B. PROFILE
6. Read profile details from SQLite through the approved repository. Display photo or initials/avatar fallback, name, roll number, department and semester.
7. Implement Edit/Save/Cancel. Validate required fields, trim input, and save via the repository. Do not show success until the write succeeds.
8. Profile values must remain after leaving and reopening the screen and after app relaunch.
9. For photo change, use a modern supported photo-picker/document-picker flow (e.g. Activity Result API if compatible with this project). Do not request broad all-files/external-storage permission. Handle cancel, unsupported file, permission/URI access and missing-file cases. If a selected URI must remain available later, persist permission where supported or copy the image to app-private storage safely. Keep the fallback avatar valid.
10. Only add fields that are in scope or necessary. Do not create fake profile security/account features.

C. FILE HANDLING / RESOURCES
11. Add a real `.txt` or `.csv` under `app/src/main/assets/` with a small, useful campus-resource dataset. Document that this is sample/static file content for the assignment.
12. Read the file at runtime using Java I/O (UTF-8), not by copying its contents into a TextView constant. Parse/display the actual asset content on the Resources screen.
13. Handle missing/empty file, I/O failure, blank lines and malformed data without crashing. If using CSV, handle headers and missing columns and do not naively split quoted CSV values if they occur. A simple TXT file is acceptable if it is more robust for this assignment, but still visibly demonstrate reading and displaying the file.
14. Add a way to inspect the loaded contents in the Resources screen. If detail dialogs are used, they must show actual parsed data.
15. Do not request external storage permissions for reading an app-bundled asset.

D. INTEGRATION AND QUALITY
16. Keep navigation and destination selection correct for Events, Profile and Resources.
17. Check image resource sizes and avoid large uncompressed assets that make the APK unnecessarily heavy.
18. Test profile editing, picker cancellation, image fallback, empty/malformed asset behavior, event details dialog and app relaunch persistence.
19. Add or update tests where practical. Run build/tests and fix regressions.

ACCEPTANCE TESTS:
- Event cards show name/date/venue/image; View Details opens correct details.
- Profile fields save to SQLite and survive navigation/relaunch.
- Photo selection/cancel/fallback is safe and does not require broad storage permission.
- Resources screen shows actual data loaded from the bundled TXT/CSV file at runtime.
- Missing/invalid asset does not crash the app.
- All new features are reachable and styled consistently.

Stop before Phase 7. Report actual test results and any remaining manual checks.
```

### Exit criteria
- [ ] Events and detail dialog function.
- [ ] Profile reads/writes SQLite.
- [ ] Photo picker/fallback works or limitations are explicit and tested.
- [ ] App reads and displays actual bundled TXT/CSV content.
- [ ] No broad storage permission is added.
- [ ] Build/tests pass.

---

## PHASE 7 — Integrate real Google Maps and campus markers

### Goal
Use the Google Maps SDK for Android correctly, with safe API-key configuration and at least three named campus locations. Be honest about what needs a real key and verified campus coordinates.

### Copy-paste prompt

```text
You are implementing Phase 7 of CampusMate. Read the master plan, AGENTS.md, implementation plan, current Gradle DSL, manifest, and Campus Map screen first. Keep the existing screen design; this phase is about real map behavior and setup, not a visual redesign.

SCOPE: implement Google Maps SDK for Android and a working map screen with at least three named location markers. Do not use a static image or WebView in place of the SDK. Do not fabricate campus coordinates or commit an API key.

1. Inspect existing dependencies and Gradle DSL. Add only the compatible Google Maps SDK dependency necessary for this project. Do not blindly upgrade Gradle/AGP or change Groovy to Kotlin DSL (or the reverse).
2. Integrate the map using the project’s architecture/lifecycle correctly (e.g. SupportMapFragment or an appropriate Maps view). Ensure Fragment/Activity lifecycle handling is correct and map callbacks do not reference destroyed views.
3. Store all campus marker definitions in one `CampusLocationRepository`/configuration class: stable ID, display name, descriptive detail, latitude and longitude. The screen must render at least three markers with readable names in marker titles/snippets and in a visible list/legend or selection UI.
4. Use real coordinates supplied by me or verified from an authoritative source. Do not invent “real” Library/Block/Cafeteria coordinates. If coordinates are not yet available, wire the data structure and give me a precise request for the required values; do not claim location accuracy. Make it easy to update coordinates in exactly one place.
5. Configure the Maps API key without hardcoding it into tracked source. Respect the current build-file DSL. Prefer reading `MAPS_API_KEY` from local-only configuration such as `local.properties`, inject it via a manifest placeholder, and document the exact setup. If an existing secure configuration method is already present, use it. Never echo an actual secret in reports. Ensure no real key is committed to Git.
6. Add only the necessary Internet/Maps manifest metadata. Do not ask for the user’s live device location permission unless “My Location” is genuinely implemented and useful. The assignment only requires campus markers; location permission is not required to fulfill that requirement.
7. Handle configuration failure, no network, empty map and unavailable Google Play services gracefully. The screen should show a clear recovery/setup state instead of crashing or showing a misleading blank success state. Do not expose an API key in UI/logs.
8. Marker taps should show useful location information. If a list/legend is present, tapping a list item should focus/select the matching map marker; marker/list selection must stay consistent. A “my current location” control is not required unless fully implemented with permission handling.
9. Ensure the Maps screen remains reachable from bottom/campus navigation and the drawer menu. Back navigation must work.
10. Add tests for location configuration validation where possible. Do not pretend an emulator unit test proves live Google map tiles render.
11. Run the project build and available tests. Then state the manual verification checklist: API key added, key’s Android package/SHA restrictions correct where applicable, Maps SDK enabled in the Google Cloud project, device online, app run, map tiles loaded, all three markers visible and selectable. Report the map as `MANUAL VERIFICATION REQUIRED` until actually verified.
12. Review Gradle/manifest diffs for accidentally committed keys or unrelated configuration changes.

ACCEPTANCE TESTS:
- App compiles with the configured map implementation.
- With a valid key and network, Google map tiles load.
- At least three correct, named location markers display and can be selected.
- Map/legend selection is useful and screen does not crash when revisited.
- Missing key/network results in a clear failure state, not fake success.
- No fabricated “real” coordinates and no committed secret.

Stop before Phase 8. Report actual build outcome and explicitly separate automated checks from live-map manual verification.
```

### Exit criteria
- [ ] Google Maps SDK is integrated, not mocked.
- [ ] API key is configured securely and setup documented.
- [ ] Three named locations have correct, confirmed coordinates.
- [ ] Live map tiles/markers manually verified or explicitly outstanding.
- [ ] App handles missing configuration/network without crashing.
- [ ] Build passes.

---

## PHASE 8 — Data-driven dashboard, global UX integration and functional consistency

### Goal
Replace temporary preview values with real data, make every flow coherent, and refine usability without adding scope creep.

### Copy-paste prompt

```text
You are implementing Phase 8 of CampusMate. Read the master plan, requirement traceability, database docs, and current implementation. This is an integration phase; do not replace architecture or redesign the whole application without a demonstrated defect.

SCOPE: connect the dashboard and navigation summaries to real repositories; unify global refresh, loading/empty/error patterns, form feedback, date formatting, accessibility and visual consistency. Preserve all verified features.

1. Remove all temporary hardcoded dashboard counts, sample attendance percentages, fake upcoming-assignment data, fake notification counters, and placeholder status claims. Dashboard figures must be derived from SQLite repositories and real local event data. If records are empty, display helpful empty states and actions.
2. Define exactly what every number means. Because this app does not have an institutional timetable, do not label a ratio of recorded rows as official attendance. Use precise language such as “attendance records saved” or an accurately defined “recorded attendance rate.” If a metric is not defensible, remove it.
3. Implement a simple, lifecycle-safe refresh strategy: after returning from saving attendance, assignment, or profile, dashboard and lists should show current database content. Re-query sources rather than trusting stale in-memory copies. Avoid unnecessary polling or background services.
4. “Next up” should select the nearest incomplete assignment from persisted data using the agreed sort rules, or show an empty-state action. Do not show past completed work as a future due item.
5. Quick actions on the dashboard must navigate to the correct form/screen, not merely highlight a tab. Events preview must open the correct event/details destination.
6. Audit every screen for visual system consistency: colors, text hierarchy, spacing, field states, buttons, list rows, icons, empty/error/loading components, status wording, date format, selected navigation state, and toolbar behavior. Correct inconsistent elements through shared tokens/components rather than one-off hardcoded colors.
7. Audit every visible control in every screen. For each button, menu item, checkbox, radio, Spinner, edit action, detail link, icon and row tap, record expected behavior and verify the implementation. Remove or implement misleading affordances; do not leave dead controls.
8. Add safeguards for duplicate taps and repeated saves where appropriate. Disable a save button while a write is in flight if the current UI architecture allows it safely. Preserve form input on failure.
9. Audit accessibility: content descriptions, input labels, text/background contrast, touch targets, focus order, text wrapping, system font scaling, and keyboard behavior. Do not rely on red/green alone to communicate status.
10. Verify logout/session behavior after process/app restart. Avoid showing private student details behind the login screen after logout. Avoid storing any password or unnecessary personal data.
11. Do not add notifications, analytics, cloud sync, registration, fabricated map features, or other scope expansions. Fix the required product rather than adding features.
12. Run unit/instrumentation tests, build, then perform the manual end-to-end test checklist. Fix issues and re-run failed checks.

ACCEPTANCE TESTS:
- Dashboard values match persisted data, and empty states are correct for a clean install.
- Quick actions navigate to the correct flows.
- After saving data, relevant screens/dashboard update immediately or on the next visible refresh.
- No fake metrics or stale counts remain.
- All visible controls have a working behavior.
- The app remains visually coherent across screen sizes and keyboard states.
- Logout does not leak the signed-in UI/session.

Stop before Phase 9. Provide a control-by-control integration report and test evidence.
```

### Exit criteria
- [ ] Dashboard derives information from repositories.
- [ ] No fake statistics or false success states remain.
- [ ] Cross-screen refresh behavior works.
- [ ] All visible controls have been audited.
- [ ] Build/tests pass.

---

## PHASE 9 — Full acceptance QA, bug fixing, build verification and submission handoff

### Goal
Test the app as a student and evaluator would. Fix defects instead of adding new features. Produce an honest test report and a clean handoff.

### Copy-paste prompt

```text
You are implementing Phase 9: final QA and submission readiness for CampusMate. Read the full master plan, AGENTS.md, traceability matrix, implementation/data-layer docs, and the actual code before changing anything.

RULE: This is a stabilization phase. Do not add new features or redesign screens unless necessary to fix an identified usability/accessibility defect. Do not remove or weaken required functionality to get a passing build.

A. BUILD AND STATIC QUALITY
1. Inspect repository status and current build. Run the project’s actual Gradle wrapper, at minimum `./gradlew assembleDebug`; run relevant unit tests and connected/instrumentation tests if an emulator is available.
2. Inspect compiler errors, resource/XML inflation issues, Android manifest, dependencies, duplicate resources, unresolved IDs, dead methods, accidental placeholder code, and meaningful lint warnings. Fix root causes and rerun the checks.
3. Search for TODO/FIXME, “coming soon”, dummy buttons, hardcoded dashboard metrics, false success messages, empty click listeners, fake maps, unconnected form controls, and hardcoded secrets. Explain each remaining instance and remove any that violate requirements.
4. Check Git diffs and configuration for leaked API keys, passwords, local file paths, or generated build clutter. Never print secrets.

B. REQUIRED END-TO-END MANUAL TEST MATRIX
Create/update `docs/FINAL_QA_REPORT.md` and check each test as PASS / FAIL / MANUAL VERIFICATION REQUIRED with steps and evidence.

TEST 1 — Clean launch: clear app data/install fresh, launch app, confirm splash-to-login behavior and no crash.
TEST 2 — Login validation: blank fields, invalid email where email is expected, blank password; confirm useful validation. Enter valid local demo values; confirm main shell opens. Confirm no password is stored.
TEST 3 — Navigation: open Home, Attendance, Assignments, Events, Map, Profile, Resources and Logout from their advertised routes. Re-tap current destination; confirm no duplicate screen/back-stack issue. Test back navigation.
TEST 4 — Attendance validation: try missing subject/status; confirm no row is saved. Select subject/date/status, save, check success feedback and history. Change status for same subject/date and confirm documented update behavior. Leave/reopen the screen; then fully relaunch and verify data persists.
TEST 5 — Assignment validation: try blank title/subject; confirm rejection. Add assignment with due date and incomplete state. Verify list order/status. Toggle complete, leave/reopen, and relaunch; verify persistence. Check multiple items and long titles.
TEST 6 — Profile: edit all required fields, save, navigate away/back and relaunch. Verify data persists. Test photo selection and cancel/fallback where implemented.
TEST 7 — Events: confirm each sample event has image, name/date/venue and View Details opens matching information. No broken image.
TEST 8 — File handling: verify Resources reads and shows the real bundled asset content. Confirm empty/missing/malformed-file handling does not crash (through tests or controlled test input).
TEST 9 — Google Maps: after valid API setup, verify real Google Maps tiles, at least three confirmed campus marker locations, labels/details, and map selection on an online emulator/device. If no valid key/coordinates are available, mark MANUAL VERIFICATION REQUIRED and state the blocker exactly; do not mark PASS.
TEST 10 — UI resilience: test small emulator size, scroll, open keyboard, long labels, empty lists, font scaling if practical, button tap targets, contrast, and screen rotation behavior if rotation is supported.
TEST 11 — Logout: confirm confirmation dialog, clear session, return to Login, and ensure user profile/academic screens are not accessible through Back without signing in.
TEST 12 — Persistence: verify Student, Attendance, Assignment records are stored in SQLite and survive app process/app restarts. Do not use screen appearance alone as proof; query through repository/database test or controlled inspection where practical.

C. BUG FIX LOOP
For every failure:
- Record reproduction steps and expected vs actual outcome.
- Identify the root cause before modifying code.
- Apply the smallest coherent fix.
- Run the narrow relevant test and then the full build/test suite.
- Re-check flows affected by the same class/layout/repository.
- Do not mark PASS until retested.

D. FINAL DELIVERY
1. Update `docs/REQUIREMENTS_TRACEABILITY.md` with evidence-based statuses.
2. Create/update `docs/FINAL_QA_REPORT.md` with environment, commands, actual build/test output, end-to-end results, screenshots requested for final evidence, and known limitations.
3. Write `docs/DEMO_SCRIPT.md` with a 2–3 minute demonstration order: launch/login → dashboard → mark attendance → add and complete assignment → events details → map markers → edit profile → resources file content → logout. Include what should be visible at each stop.
4. Do not claim the app is production-ready if it uses local demo login, sample event content, and locally configured map credentials. Describe the app as an assignment/demo implementation with those limitations.
5. State exactly whether the final build passed and list every requirement that is still incomplete or requires manual verification. Do not conceal failures.

STOP after final report. Do not start any new phase.
```

### Exit criteria
- [ ] Clean Gradle build passes.
- [ ] All feasible tests run and their real results are documented.
- [ ] Every required screen/control is covered by a traceability entry.
- [ ] Full manual test matrix is honestly marked.
- [ ] Maps is only marked PASS after a live test with real coordinates/key.
- [ ] `docs/FINAL_QA_REPORT.md` and `docs/DEMO_SCRIPT.md` exist.

---

# PART D — Reusable repair and review prompts

## 1. Repair Prompt — when a build fails or a feature breaks

```text
Stop feature development. Read `CampusMate_Gemini_Master_Prompts.md` and `AGENTS.md`, then diagnose this specific failure. Do not perform a broad refactor.

OBSERVED FAILURE:
[Paste complete relevant error/log or exact reproduction steps here.]

EXPECTED BEHAVIOR:
[Describe what should happen.]

CONTEXT:
[Name the screen, action, Android version/emulator, and latest phase changed.]

Required workflow:
1. Reproduce or isolate the failure using the actual project state.
2. Identify the earliest meaningful error and trace its root cause; do not chase downstream symptoms first.
3. Inspect callers, IDs, layouts, resources, lifecycle, Gradle config and database contracts involved.
4. Explain the root cause in plain language before applying a minimal targeted patch.
5. Preserve Java + XML Views, SQLite and all other master-plan constraints.
6. Add or update a regression test if possible; otherwise give a precise manual test.
7. Run the narrow test/build, then `./gradlew assembleDebug` and other relevant tests. Fix any new regressions.
8. Inspect the diff for unrelated changes, dead code, secrets, and changes that weaken requirements.
9. Report changed files, commands and actual results. If unable to run a check, state that explicitly.
Do not continue to the next phase until the failure is fixed or I approve a documented workaround.
```

## 2. UI/UX Critique Prompt — before approving a screen

```text
Act as a senior mobile product designer and Android accessibility reviewer. Review the current CampusMate screen from first principles, but do not change code yet.

For this screen, identify:
1. The student’s primary job-to-be-done.
2. The primary action and whether it is visually obvious.
3. Unnecessary taps, competing actions, unclear labels, or misleading metrics.
4. Hierarchy, spacing, alignment, reading order, contrast, typography, touch targets, and keyboard/scroll behavior.
5. Empty/loading/error/success/disabled/selected states that are missing.
6. Whether the design fits the unique Campus Atlas identity and avoids copying the supplied reference.
7. Whether a visible interaction is actually implemented, and the behavior needed if not.
8. Edge cases: no data, long text, duplicate saves, due date in the past, narrow screen, larger font size.

Return a prioritized list of only the most important problems, each with expected behavior and measurable acceptance criteria. Do not praise or redesign for decoration alone. Do not implement until I approve the critique.
```

## 3. Requirement audit prompt — after any phase

```text
Audit the actual implementation against `CampusMate_Gemini_Master_Prompts.md`, `AGENTS.md`, and `docs/REQUIREMENTS_TRACEABILITY.md`.

Do not infer completion from the existence of a class or XML file. For every relevant requirement, inspect the code path and state:
- Requirement
- File/method/resource responsible
- Evidence of working behavior or test
- Status: PASS / FAIL / MANUAL VERIFICATION REQUIRED / NOT IMPLEMENTED
- Exact next action if not PASS

Specifically search for static/fake data masquerading as live data, controls without listeners, success messages that do not check operation results, persistence that only exists in memory, missing validation, unsafe SQL, stale dashboard values, fake map implementations, unconfigured map API key, broken navigation, and required control types that are present but unused.

Run the build and relevant tests. Update the traceability document only with evidence-backed statuses. Report failures plainly and do not start another phase.
```

## 4. “Do not redesign everything” prompt — when Gemini starts rewriting too much

```text
Pause. Your proposed change is broader than the defect requires. Do not apply a whole-project rewrite.

Inspect the diff and current implementation. Identify the smallest set of files and lines needed to fix the stated issue while preserving the master plan, visual system, navigation contract, database contract, and working features. Keep existing IDs and public method contracts unless a change is required. Explain any unavoidable migration or dependency change before proceeding. Apply only the minimal patch, then run build/tests and report regressions.
```

---

# PART E — Final evaluator checklist and demo plan

## Assignment requirement checklist

| Requirement from the brief | Evidence expected in the final app |
|---|---|
| Splash screen | App name/logo appears briefly; reliably routes onward |
| Login | Username/email + password + Login; validation and working Home navigation |
| Dashboard | Welcome message and working access to main functions; dynamic data where applicable |
| Attendance | Subject Spinner, DatePicker, Present/Absent RadioButtons, Save action, persisted records |
| Assignments | Name/title, subject, due date, Completed CheckBox, save, visible persisted list |
| Events | Name, date, venue, image, working View Details dialog/view |
| Campus Map | Actual Google Maps SDK, three named and verified location markers |
| Profile | Photo/fallback, name, roll number, department, semester; SQLite persistence |
| Menu/navigation | Home, Attendance, Assignments, Events, Map, Profile, Logout all accessible |
| Required controls | TextView, EditText, Button, ImageView, CheckBox, RadioButton, Spinner, DatePicker, Toast, AlertDialog, navigation are used in context |
| SQLite | Student information, attendance and assignment data persist after relaunch |
| File handling | Real bundled TXT/CSV is read at runtime and displayed |
| Interactivity | Buttons cause the promised behavior; invalid inputs/errors are handled |
| Distinct design | Campus Atlas identity, original hierarchy and consistent component system; not a clone of the reference |

## Recommended live demo order (2–3 minutes)

1. Open app and show splash/login. Use clearly identified demo credentials, not a real password.
2. Enter the dashboard and explain how “Next up” and summaries use saved data rather than fixed mock values.
3. Open Attendance, choose a subject, set date/status, save, and show the new history row.
4. Open Assignments, add one task with a due date, save, then mark it completed.
5. Navigate away and back (or relaunch if time allows) to prove persistence.
6. Open Events and show View Details.
7. Open Campus Map and demonstrate three verified markers. Ensure the API key/network/coordinates have been tested before the demo.
8. Open Profile, edit a field and save; visit Resources and show loaded file content.
9. Show the navigation menu and Logout flow.

## Screenshot evidence to capture

- Splash/login (one is enough if splash is transient and hard to capture).
- Full dashboard with real empty/sample-state behavior documented.
- Attendance form and saved attendance history.
- Assignments form/list, including completed state.
- Events screen and details dialog.
- Map screen with all three markers visible.
- Profile display/edit state.
- Resources screen showing actual TXT/CSV content.
- Navigation drawer/menu and logout confirmation.
- Optional: SQLite/data persistence verification and the final passing build/test report.

## Final non-negotiable quality bar

A requirement is not complete because Gemini says it is complete. Verify the user-visible behavior. The objective is a **distinctive, coherent, fully working assignment app**, not a large codebase or a screenshot-only prototype. Prioritize correctness, predictable flows, usable design, and truthful test evidence over additional features.
