# CampusMate (NMIMS Chandigarh) — Backend API Gap Analysis & Proposed Contracts

> **IMPORTANT NOTICE:** 
> The current Android Studio project archive contains **NO IMPLEMENTED REMOTE HTTP NETWORK CLIENT OR API ENDPOINTS**. 
> Registration and sign-in currently work against a **device-local account store only**. There is no remote account verification or sync. All API endpoints, request payloads, and response structures listed below are **PROPOSED CONTRACTS FOR A BACKEND OWNER** and do **NOT** exist in the current client codebase.

---

## 1. Network Architecture Audit Findings

* **Network Client Status:** `ABSENT`. The codebase contains no network client dependencies (no Retrofit, OkHttp, Volley, Ktor, or HttpURLConnection).
* **Authentication Engine:** `DEVICE-LOCAL ONLY`. Registration stores a salted PBKDF2 password verifier in SQLite and sign-in checks it locally. The `@nmims.in` suffix is not verified with the institution. No remote identity verification, reset, token, or sync exists.
* **Remote Token / Password Reset:** `UNIMPLEMENTED`. No server token refresh, email verification link handling, or remote password reset exists. Local password reset is disabled because SAP ID and email alone cannot prove email ownership.
* **Official University Records:** `UNIMPLEMENTED`. No live server feed for institutional attendance or course enrollment exists.

---

## 2. Proposed API Endpoint Contracts for Backend Owner

| Feature / Capability | Proposed Endpoint Method & Path | Proposed Request Payload (Client-to-Server) | Proposed Response Payload (Server-to-Client) | Assumptions & Flags requiring Backend Approval | Status |
|---|---|---|---|---|---|
| **Student Registration** | `POST /api/v1/auth/register` | `{ "name": "Full Name", "sap_id": "700112233", "email": "student@nmims.in", "year": 3, "department": "STME", "password": "password123" }` | `{ "status": "success", "message": "Verification email sent", "student_id": "1001" }` | *Note: Password confirmation match is handled client-side. SAP ID length (numeric 6-12) and password complexity require backend policy confirmation.* | `PROPOSED CONTRACT — NOT IMPLEMENTED` |
| **Student Authentication** | `POST /api/v1/auth/login` | `{ "sap_id_or_email": "700112233", "password": "password123" }` | `{ "access_token": "jwt...", "student": { "sap_id": "700112233", "name": "...", "email": "...@nmims.in", "department": "STME", "year": 3 } }` | *Note: No name field at sign-in. Token format, TTL, and refresh mechanism require backend specification.* | `PROPOSED CONTRACT — NOT IMPLEMENTED` |
| **Forgot Password** | `POST /api/v1/auth/forgot-password` | `{ "email": "student@nmims.in" }` | `{ "status": "success", "message": "If an account exists, a reset link has been sent" }` | *Note: Neutral non-enumerating response format. One-time expiring reset token flow requires backend server implementation.* | `PROPOSED CONTRACT — NOT IMPLEMENTED` |
| **Official Class Session Logs** | `GET /api/v1/attendance/sessions` | Header: `Authorization: Bearer <token>` | `{ "department": "STME", "threshold_percent": 80, "subjects": [ { "subject_code": "CS301", "subject_name": "Data Structures", "total_conducted": 20, "total_attended": 18 } ] }` | *Note: Session status semantics (present/absent/late/excused), time zone, and cancellation policies require backend specification.* | `PROPOSED CONTRACT — NOT IMPLEMENTED` |
| **Course Assignments Feed** | `GET /api/v1/assignments` | Header: `Authorization: Bearer <token>` | `{ "assignments": [ { "id": "asgn_1", "title": "Lab 3", "subject": "Data Structures", "due_date": "2026-10-25", "is_completed": false } ] }` | *Note: Assignment issuance ownership (instructor vs student-created) requires backend contract definition.* | `PROPOSED CONTRACT — NOT IMPLEMENTED` |
| **Campus Events Feed** | `GET /api/v1/events/nmims-chandigarh` | None | `{ "events": [] }` | *Note: If no live event feed exists, client renders clean empty state and official portal link.* | `PROPOSED CONTRACT — NOT IMPLEMENTED` |
| **Campus Resources Feed** | `GET /api/v1/resources/nmims-chandigarh` | None | `{ "resources": [] }` | *Note: Currently parsed locally from app-bundled asset `campus_resources.csv` using Java I/O.* | `PROPOSED CONTRACT — NOT IMPLEMENTED` |

---

## 3. Security & Non-Negotiable Integration Rules

1. **No Password Payload Leakage:** Password confirmation matching is performed client-side. The server request payload carries only `password`.
2. **Neutral Non-Enumerating Reset:** `POST /api/v1/auth/forgot-password` returns an identical success message whether or not the email exists in the database.
3. **Institutional Email Enforcement:** Client validates strict `@nmims.in` domain before any network submission.
4. **HTTPS Encryption:** All remote API traffic must travel strictly over TLS/HTTPS. Passwords and access tokens are never logged or stored in persistent unencrypted files.
