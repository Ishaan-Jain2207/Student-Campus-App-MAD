# Acadia — SVKM's NMIMS Chandigarh Student App

Acadia is a native Android application designed for students of **SVKM's NMIMS Chandigarh** (School of Technology Management & Engineering (STME), School of Commerce (SOC), School of Law (SOL)). It provides a personal academic organization hub including semester-wise attendance estimation, assignment task tracking with priority management, document resource storage, multi-campus map navigation, and student profile management.

---

## Key Features

### 1. Account Authentication & Profile Identity
* **Institutional Registration:** Client-side validation enforcing numeric SAP IDs, institutional `@nmims.in` email addresses, academic year (1–5), and constituent department choices (`STME`, `SOC`, `SOL`).
* **Local Authentication:** Password hashing using PBKDF2 with SHA-1 (`PBKDF2WithHmacSHA1`) and salt stored locally in SQLite.
* **Student Identity:** Read-only institutional fields (SAP ID, Email, Department, Year) with user-editable name and persistent profile avatar storage.

### 2. Personal Attendance Tracker & Semester Management
* **Academic Period Management:** Create, select, and switch between academic semesters (`academic_periods`).
* **Subject-Wise Logging:** Add personal subjects per semester and record class attendance entries with status (`PRESENT`, `ABSENT`, `CANCELLED`).
* **Faculty Name Support:** Optional faculty name tracking for each personal subject.
* **Advisory Department Benchmarks:** Automatically calculates attendance percentages excluding `CANCELLED` classes and evaluates advisory benchmarks based on department (`STME`: 80%, `SOC`: 80%, `SOL`: 70%).
* **Clear Personal Disclaimer:** Prominently states that logged entries are personal student records and not official university records.

### 3. Assignments & Task Manager
* **Task Management:** Create, track, and complete personal assignments linked to personal subjects and academic semesters.
* **Priority Levels:** Assign priority levels (**Low**, **Medium**, **High**) using radio button selection.
* **Temporal Status Badges:** Dynamic task status indicators (`Upcoming`, `Due Today`, `Overdue`, `Completed`) based on due dates.

### 4. Multi-Campus Map & Location Navigation
* **All 8 NMIMS Campuses:** Interactive Google Maps integration supporting all 8 NMIMS campuses across India (Chandigarh, Mumbai, Navi Mumbai, Bengaluru, Hyderabad, Indore, Shirpur, and Dhule).
* **Precise Coordinates:** Chandigarh campus marker pinned at `30.78346392270489, 76.76151082103206`.
* **Directions Intent & Fallback:** "Get Directions" opens Google Maps app navigation with a web browser directions fallback.
* **Current Location:** Supports runtime location permissions (`ACCESS_FINE_LOCATION`) and Google Maps location overlays.

### 5. Campus Resources & Document Management
* **Document Import:** Import personal academic documents (`.pdf`, `.txt`, `.csv`, `.docx`, etc.).
* **Subject Categorization:** Filter and organize uploaded resources by subject.
* **Text/CSV File Reader:** Built-in Java I/O text reader to preview `.txt` and `.csv` document contents directly in the app.

---

## Tech Stack & Architecture

* **Language:** Java (JDK 17)
* **Minimum SDK:** Android 7.0 (API Level 24) / **Target SDK:** API Level 37
* **UI Framework:** Android XML Views, Material Components (Material 3), Navigation Drawer, Bottom Navigation Bar
* **Dependency Injection:** Hilt / Dagger (`@AndroidEntryPoint`)
* **Local Persistence:** Native SQLite (`SQLiteOpenHelper` v12) with account-scoped tables (`owner_account_id`), foreign key constraints, and parameterized SQL queries
* **Asynchronous Processing:** `AppExecutors` with dedicated Disk I/O and Main Thread executors
* **Security:** PBKDF2 password hashing, secure file storage in `getFilesDir()`, Google Maps API key isolation in `local.properties` (untracked in Git)

---

## Project Structure

```text
app/src/main/java/com/example/ishaan_student_app/
├── AcadiaApplication.java            # Hilt Application Class
├── data/
│   ├── db/DatabaseHelper.java        # SQLite OpenHelper (v12 schema & migrations)
│   ├── file/ResourceFileReader.java  # Java I/O CSV/Text file parser
│   ├── model/                        # Data models (Student, Assignment, AttendanceEntry, etc.)
│   ├── repository/                   # SQLite Repositories (Student, Account, Content, etc.)
│   └── session/SessionManager.java   # SharedPreferences session manager
├── ui/
│   ├── assignments/                  # Tasks UI & Priority Adapter
│   ├── attendance/                   # Semester & Attendance Tracker UI
│   ├── events/                       # Campus Events UI
│   ├── home/                         # Daily Briefing Dashboard UI
│   ├── login/                        # Sign-In Activity
│   ├── main/MainActivity.java        # Main Shell (Drawer + Bottom Nav)
│   ├── map/CampusMapFragment.java    # Google Maps SDK Fragment
│   ├── profile/ProfileFragment.java  # Student Profile UI
│   ├── register/RegisterActivity.java# Registration Activity
│   ├── resources/                    # Subject-Wise Document Resources UI
│   └── splash/SplashActivity.java    # Animated Splash Launcher Activity
└── util/                             # PasswordHasher, RegistrationValidator, AppExecutors
```

---

## Setup & Local Installation

### Prerequisites
* **Android Studio:** Ladybug (2024.2.1) or newer
* **JDK:** Java 17
* **Android SDK:** API Level 34 or 37 installed

### Environment Configuration

1. Clone the repository:
   ```bash
   git clone https://github.com/Ishaan-Jain2207/Student-Campus-App-MAD.git
   cd Student-Campus-App-MAD
   ```

2. Create a `local.properties` file in the root directory if it does not exist, and specify your Android SDK path and Google Maps API Key:
   ```properties
   sdk.dir=/path/to/your/android/sdk
   MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY_HERE
   ```

3. Open the project in Android Studio, sync Gradle, and run:
   ```bash
   ./gradlew assembleDebug
   ```

---

## Running Unit Tests

Run local JVM unit tests from the command line:
```bash
./gradlew testDebugUnitTest
```

---

## License & Attribution

Developed for **SVKM's NMIMS Chandigarh**. All institutional trademarks, school names (`STME`, `SOC`, `SOL`), and campus details belong to SVKM's NMIMS.
