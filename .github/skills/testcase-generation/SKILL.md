# Skill: Requirement Understanding + Test Case Generation

## Purpose
This combined skill covers the **full pipeline** from reading a user story to producing a structured JSON test case file and auto-generating its Excel output.

**Phase 1 — Requirement Understanding**: Read and analyse the user story, extract acceptance criteria (ACs), identify actors, and map each AC to an API contract using real POJO field names.

**Phase 2 — Test Case Generation**: Convert the extracted requirements into a JSON test case file (`<UserStoryName>.json`) and auto-generate the matching Excel file (`<UserStoryName>.xlsx`).

---

## Workflow

```
User Story Input
      ↓
[Phase 1] Read user story → extract ACs + actors + API contracts
      ↓
[Phase 2a] Create <UserStoryName>.json       ← write JSON file first
      ↓
[Phase 2b] Run TestCaseGenerator             ← generate <UserStoryName>.xlsx immediately after JSON
      ↓
  ✅ STOP — do NOT proceed to code generation until BOTH JSON and Excel exist
```

> ⚠️ **STRICT ORDERING RULE**
>
> The JSON file and its Excel output MUST both be created and verified **before** any automation
> test class is written.  
> Phase 2b (Excel generation) is NOT optional — it MUST run immediately after Phase 2a (JSON creation).
>
> **Correct order:**  
> `JSON created` → `Excel generated` → `test class written`
>
> **Wrong order (do NOT do this):**  
> ❌ `JSON created` → `test class written` → `Excel generated`

---

# PHASE 1 — Requirement Understanding

## Step 1: Read the User Story

- Read the user story provided by the user (file path, pasted text, or attached document).
- Extract only the **functional acceptance criteria** that describe what the user wants to achieve.
- Identify all **actors** (e.g., Admin, Recruiter, Candidate) and their permitted actions.
- **Ignore** generic pre-conditions like "user must be logged in" — authentication is handled by `BaseTest`.

---

## ⚠️ GENERIC NAMING RULE — MANDATORY for all JSON and Excel test cases

> **NEVER use real person names** (e.g. Manik, Prajwal, Omkar, Jaydeep, Smitha) in any test case content.  
> Always use **generic role-based identifiers** instead.

| Instead of | Use |
|---|---|
| Manik, Prajwal, Omkar, John, Jane... | `Candidate1`, `Candidate2`, `Candidate3` |
| Jaydeep, Smitha, Bob... | `Recruiter1`, `Recruiter2` |
| Admin User, Super Admin... | `Admin1` |
| Any real person's name in fullName, coverLetter, description | Generic placeholder |

**This rule applies to:**
- `requestBody` values in JSON steps (e.g. `fullName`, `coverLetter`)
- `responseBody` values in JSON steps (e.g. `fullName`, `candidate.fullName`)
- Step descriptions in JSON and Excel
- Any email `fullName` fields

**Correct examples:**
```json
{ "fullName": "Candidate1", "coverLetter": "I am Candidate1 applying for this role." }
{ "fullName": "Recruiter1", "role": "RECRUITER" }
{ "fullName": "Admin1", "role": "ADMIN" }
```

**Wrong examples:**
```json
{ "fullName": "Manik Candidate" }       ← ❌ real name
{ "coverLetter": "I am Jaydeep..." }    ← ❌ real name
```

> ✅ Email addresses from `userDetails.properties` (e.g. `manik@test.com`) MAY appear in responses  
> since they are system-defined, but `fullName` fields must always use generic identifiers.

---

## Step 2: Think from a High-Level API Perspective

For each **business action** described in the user story, determine:

- What **HTTP method** is needed (GET, POST, PUT, DELETE)?
- What **endpoint** will be called?
- What **request payload** is required?
- What **response** is expected (status code + business data)?
- What **pre-conditions** are specific to this story (not generic auth)?

> **Focus on what the user story says** — not on generic infrastructure concerns.

---

## Step 3: Derive Payload from POJOs

Navigate to `src/main/java/com/hiring/pojo/` and inspect the relevant POJO:

| POJO Class | Purpose |
|---|---|
| `ApplicationRequestPOJO.java` | Payload for applying to a job |
| `CandidatePOJO.java` | Candidate profile data |
| `JobRequestPOJO.java` | Payload for creating/updating a job |
| `LoginRequestPOJO.java` | Payload for user login |
| `RegisterRequestPOJO.java` | Payload for user registration |
| `UserProfileRequestPOJO.java` | Payload for updating user profile |

Map each user story action to the corresponding POJO to get exact field names — do **NOT** guess.

---

## Step 4: High-Level Test Case Categories

| Category | Include? | Description |
|---|---|---|
| **Happy Path** | ✅ Always | The main success flow described in the user story |
| **Business Negative** | ✅ Yes | Failures relevant to the story (e.g., duplicate apply → 400, invalid jobId → 404) |
| **Authorization (role-based)** | ✅ If story mentions roles | e.g., Recruiter cannot apply, Candidate cannot post jobs |
| **E2E Flow** | ✅ Always | Comprehensive multi-step test covering the full user story |
| **Invalid credentials** | ❌ Exclude | Login with wrong password — NOT part of the user story |
| **Missing mandatory fields** | ❌ Exclude | Generic field validation — NOT part of the user story |
| **Auth token missing** | ❌ Exclude | Generic 401 tests — NOT part of the user story |

---

## Step 4a: E2E Test Case — Generation Mode Rules

The number of E2E test cases generated depends on **what the user asks for**:

### Mode 1 — "Create high-level test cases" / "One test case"
> Generate **exactly 1 E2E test case** that covers the complete user story end-to-end.

- Each step = one full business action (login → search → extract → apply → verify)
- Covers every AC in sequence in a single flow
- Named plainly: e.g., `E2E: Search, Apply and View Dashboard`
- ❌ Do NOT split into per-AC test cases

### Mode 2 — "Cover all scenarios" / "Complex scenarios" / "All possible test cases"
> Generate **multiple high-level test cases** — one per AC + one E2E at the end.

- Each AC gets its own test case (e.g., Apply by Company, Apply by Title, Apply by Location, Apply by Salary)
- Each AC test case follows the **full apply flow**: login → search by that parameter → extract jobId → apply → verify dashboard
- Business negative cases included: duplicate apply → 409, apply for non-existent job → 404
- The **last test case is always E2E** — a single flow that chains all ACs together
- All test cases remain **high-level** — no field-level assertion tests

**E2E Step Structure (reference for US-HIREFLOW-001):**
```
Step 1: Login                                  → POST /api/auth/login
Step 2: Search jobs by parameter (AC-1..AC-4)  → GET  /api/jobs?<param>=...
Step 3: Extract jobId from search response     → (data extraction)
Step 4: Apply for the job                      → POST /api/applications
Step 5: Verify application in dashboard        → GET  /api/applications/my
```

---

## Step 5: Requirement Output Format

For each identified requirement produce:

```
### REQ-XXX: [Requirement Title]
- **Actor**: [Admin / Recruiter / Candidate]
- **Action**: [What the user does — directly from the story]
- **API Endpoint**: [HTTP Method] /api/endpoint
- **Auth Required**: [Role / Token type]
- **Request Payload (from POJO)**: [POJO class name + key fields]
- **Expected Response**: [Status code + key business response fields]
- **Pre-conditions**: [Business pre-conditions only — not generic auth]
- **Acceptance Criteria**: [From user story]
- **Test Scenarios** (high-level only):
  - Happy path: ...
  - Business negative: ...
  - E2E step: ...
```

---

## Phase 1 Rules

- ✅ Always read the user-provided user story first.
- ✅ Always reference POJO classes for payload fields — never guess field names.
- ✅ Only generate test cases **directly described in or derived from the user story**.
- ✅ **Always include at least ONE E2E test case** as the last test case.
- ✅ The E2E test case must be **high-level** — one step per business action, not per field.
- ✅ **Mode 1** (high-level / one test case) → generate 1 E2E test case only.
- ✅ **Mode 2** (all scenarios / complex) → one test case per AC + business negatives + 1 E2E at the end.
- ✅ Identify API dependencies (e.g., need jobId from search before applying).
- ❌ Do NOT generate: login with invalid password, register with duplicate email, missing auth token, missing mandatory fields.
- ❌ Do NOT generate test cases for features NOT mentioned in the user story.
- ❌ **NEVER generate test cases for Admin deleting a Candidate** (`DELETE /api/admin/users/{id}`) — Admin deleting any user (Candidate, Recruiter, etc.) is excluded from all test case generation regardless of user story content, supplementary sections, or system behavior summaries.

---

# PHASE 2 — Test Case Generation

## Step 0: Read Java Source Classes First (MANDATORY)

Before generating any test case, **read every class** inside `src/main/java/com/hiring/` to extract the real API contract:

| Package | Class | What to extract |
|---|---|---|
| `utils/` | `URLGenerator.java` | All endpoint constants → use as URLs in steps |
| `pojo/` | `*RequestPOJO.java` | All private fields → use as request body keys |
| `response/` | `*Response.java` | All private fields → use as response body keys |
| `helpers/` | `ActorHelper.java` | Method ↔ endpoint mapping + HTTP verb |

> ✅ **Every time you generate test cases, re-read the class files** to pick up any newly added endpoints or fields.
> ✅ If a new POJO or endpoint is found that is not in the table below, add it to your test cases.

---

### 🔴 MANDATORY — Read `userDetails.properties` Before Writing Any Testdata JSON

**Always read `src/main/resources/testdata/userDetails.properties`** before creating the per-test testdata JSON file.  
The key prefixes in the testdata JSON MUST match the prefixes defined in `userDetails.properties`.

**Actual User Registry (source of truth — read this file, never guess):**

| Prefix | Actual Email | Password | Role | fullName to use in testdata JSON |
|---|---|---|---|---|
| `admin` | `admin@test.com` | `admin123` | ADMIN | `Admin1` |
| `recruiter1` | `jaydeep@test.com` | `pass123` | RECRUITER | `Recruiter1` |
| `recruiter2` | `smitha@test.com` | `pass123` | RECRUITER | `Recruiter2` |
| `candidate1` | `omkar@test.com` | `pass123` | CANDIDATE | `Candidate1` |
| `candidate2` | `prajwal@test.com` | `pass123` | CANDIDATE | `Candidate2` |
| `candidate3` | `manik@test.com` | `pass123` | CANDIDATE | `Candidate3` |

**Rules:**
- ✅ Testdata JSON key prefix (e.g. `candidate1.fullName`) MUST match the prefix in `userDetails.properties` (e.g. `candidate1.email=omkar@test.com`)
- ✅ `fullName` in testdata JSON uses the **generic role-based name** — `Candidate1`, `Recruiter1`, `Admin1` — NOT the email username
- ❌ Email and password are NEVER written in testdata JSON — they come from `tokenManager.getEmail/getPassword("prefix")` at runtime
- ❌ Do NOT invent new prefixes — only use: `admin`, `recruiter1`, `recruiter2`, `candidate1`, `candidate2`, `candidate3`

**Correct testdata JSON key structure (aligned with `userDetails.properties`):**
```json
{
  "admin.fullName":      "Admin1",
  "admin.phone":         "9000000001",
  "admin.role":          "ADMIN",

  "recruiter1.fullName": "Recruiter1",
  "recruiter1.phone":    "9000000002",
  "recruiter1.role":     "RECRUITER",

  "candidate1.fullName": "Candidate1",
  "candidate1.phone":    "9000000004",
  "candidate1.role":     "CANDIDATE"
}
```
> `candidate1` prefix → binds to `candidate1.email=omkar@test.com` in `userDetails.properties`  
> `recruiter1` prefix → binds to `recruiter1.email=jaydeep@test.com` in `userDetails.properties`

---

### Extracted API Contract Reference

#### Authentication

| Method | URL | POJO / Fields | Notes |
|---|---|---|---|
| `POST` | `http://localhost:5000/api/auth/register` | `RegisterRequestPOJO`: `email`, `password`, `fullName`, `phone`, `role` | CANDIDATE or RECRUITER |
| `POST` | `http://localhost:5000/api/auth/login` | `LoginRequestPOJO`: `email`, `password` | Returns `accessToken` |

**Sample register request:**
```json
{
  "email": "johndoe@test.com",
  "password": "Pass@123",
  "fullName": "John Doe",
  "phone": "9876543210",
  "role": "CANDIDATE"
}
```
**Sample login request:**
```json
{
  "email": "johndoe@test.com",
  "password": "Pass@123"
}
```
**Sample login response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "id": 2,
    "email": "johndoe@test.com",
    "fullName": "John Doe",
    "role": "CANDIDATE"
  }
}
```

---

#### Jobs

| Method | URL | POJO / Fields | Notes |
|---|---|---|---|
| `POST` | `http://localhost:5000/api/jobs` | `JobRequestPOJO`: `title`, `description`, `location`, `company`, `salary`, `type` | Recruiter only |
| `GET` | `http://localhost:5000/api/jobs` | — (no body) | List all active jobs |
| `GET` | `http://localhost:5000/api/jobs/{id}` | — (no body) | Get job by ID |
| `PUT` | `http://localhost:5000/api/jobs/{id}` | `JobRequestPOJO`: `title`, `description`, `location`, `company`, `salary`, `type` | Recruiter only |
| `DELETE` | `http://localhost:5000/api/jobs/{id}` | — (no body) | Recruiter only |

**Sample job request:**
```json
{
  "title": "Software Developer",
  "description": "Java backend developer role",
  "location": "Bangalore, India",
  "company": "TechCorp Solutions",
  "salary": "8,00,000 - 15,00,000",
  "type": "FULL_TIME"
}
```
**Sample GET /api/jobs response:**
```json
{
  "success": true,
  "message": "Jobs retrieved",
  "data": [
    {
      "id": 1,
      "title": "Software Developer",
      "description": "Java backend developer role",
      "location": "Bangalore, India",
      "company": "TechCorp Solutions",
      "salary": "8,00,000 - 15,00,000",
      "type": "FULL_TIME",
      "active": true
    }
  ]
}
```

---

#### Applications

| Method | URL | POJO / Fields | Notes |
|---|---|---|---|
| `POST` | `http://localhost:5000/api/applications` | `ApplicationRequestPOJO`: `jobId` (int), `coverLetter` | Candidate only |
| `GET` | `http://localhost:5000/api/applications/my` | — (no body) | Candidate's own applications |
| `GET` | `http://localhost:5000/api/applications/job/{jobId}` | — (no body) | Recruiter views applicants for a job |
| `PUT` | `http://localhost:5000/api/applications/{id}/status?status=INTERVIEW_SCHEDULED` | query param: `status` | Status values: **INTERVIEW_SCHEDULED, ON_HOLD, REJECTED, SELECTED** — **SELECTED triggers auto-rejection of all other applicants + job deactivation** |

**Sample application request:**
```json
{
  "jobId": 1,
  "coverLetter": "I am excited to apply for this role."
}
```
**Sample POST /api/applications response:**
```json
{
  "success": true,
  "message": "Application submitted",
  "data": {
    "id": 9,
    "job": {
      "id": 1,
      "title": "Software Developer",
      "location": "Bangalore, India",
      "company": "TechCorp Solutions",
      "salary": "8,00,000 - 15,00,000",
      "type": "FULL_TIME",
      "active": true
    },
    "candidate": {
      "id": 2,
      "email": "johndoe@test.com",
      "fullName": "John Doe",
      "role": "CANDIDATE"
    },
    "status": "PENDING",
    "appliedAt": "2026-05-28T21:49:36.493"
  }
}
```

---

#### User Profile

| Method | URL | POJO / Fields | Notes |
|---|---|---|---|
| `GET` | `http://localhost:5000/api/users/profile` | — (no body) | Get current user profile |
| `PUT` | `http://localhost:5000/api/users/profile` | `UserProfileRequestPOJO`: `fullName`, `phone`, `skills`, `experience` | Update profile |

**Sample profile request:**
```json
{
  "fullName": "John Doe",
  "phone": "9876543210",
  "skills": "Java, REST API, TestNG",
  "experience": "3 years"
}
```
**Sample GET /api/users/profile response:**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "email": "johndoe@test.com",
    "fullName": "John Doe",
    "phone": "9876543210",
    "role": "CANDIDATE",
    "skills": "Java, REST API, TestNG",
    "experience": "3 years"
  }
}
```

---

#### File Upload & Admin

| Method | URL | Notes |
|---|---|---|
| `POST` | `http://localhost:5000/api/upload/resume` | Multipart form-data, field name: `resume` |
| `GET` | `http://localhost:5000/api/admin/users` | Admin only — list all users |
| `DELETE` | `http://localhost:5000/api/admin/users/{id}` | Admin only — delete user by ID |

---

## Step 1: Map ACs to Test Cases

- Every acceptance criteria (AC) from Phase 1 must map to at least one test case in the JSON.
- Use the extracted API contract above as the single source of truth for all URLs, payloads, and response shapes.

---

## Step 2: Create the JSON File

Before creating the JSON file, **ensure the `data/` directory exists**. If it does not exist, create it:

```powershell
New-Item -ItemType Directory -Force "src\main\resources\TestCases\data"
```

Then create the JSON file at:
```
src/main/resources/TestCases/data/<UserStoryName>.json
```

> **The file name MUST be identical to the user story name** (no spaces, same case).
> **Always use `create_file` tool** to write the JSON — this ensures the file and any missing parent directories are created automatically.

---

## Step 3: JSON Schema

```json
[
  {
    "slNo": "1",
    "testCaseId": "TC_XXX_001",
    "testCaseName": "Plain action-based name",
    "testCaseDescription": "Full sentence describing what is verified",
    "updateResponseBody": "Optional: prettified JSON to patch into Response Body after generation",
    "steps": [
      {
        "step": "Step description — action + endpoint (e.g. Login: POST http://localhost:5000/api/auth/login)",
        "requestBody": "{ \"email\": \"user@test.com\", \"password\": \"Pass@123\" }",
        "responseBody": "{\n  \"success\": true,\n  \"accessToken\": \"eyJhbGci...\"\n}"
      }
    ]
  }
]
```

---

## Step 4: Step Format — Critical Rules

Each step MUST follow this pattern:

| Field | What to put |
|---|---|
| `step` | Plain English action + HTTP method + full API URL. e.g. `Login: POST http://localhost:5000/api/auth/login` |
| `requestBody` | **Prettified JSON payload** (2-space indented). Use `""` for GET requests with no body. For GETs with query params, put the full URL here. |
| `responseBody` | **Full prettified JSON response** matching real API output — include nested objects (job, candidate, status, appliedAt). Do NOT use just "200 OK". |

> ✅ Base URL: `http://localhost:5000`
> ✅ Request/Response bodies must be **prettified JSON** (2-space indented)
> ✅ Responses must reflect **realistic API output** based on POJO structure

---

## Step 5: Sample Test Case (Reference — follow this style)

```
TC_HMS_001 | Apply for a Job Based on Location
Steps:
  1. Login: POST http://localhost:5000/api/auth/login
     Request:  { "email": "johndoe@test.com", "password": "Pass@123" }
     Response: { "success": true, "data": { "accessToken": "eyJhbGci...", "role": "CANDIDATE" } }

  2. Get list of available jobs: GET http://localhost:5000/api/jobs
     Request:  http://localhost:5000/api/jobs
     Response: { "success": true, "data": [ { "id": 1, "title": "Software Developer", "location": "Bangalore, India", ... } ] }

  3. Extract jobId where location = Bangalore from step 2 response
     Request:  ""
     Response: jobId = 1 (extracted from GET /api/jobs where location = Bangalore)

  4. Apply for the job: POST http://localhost:5000/api/applications
     Request:  { "jobId": 1, "coverLetter": "I am excited to apply." }
     Response: {
                 "success": true,
                 "message": "Application submitted",
                 "data": {
                   "id": 9,
                   "job": { "id": 1, "title": "Software Developer", "location": "Bangalore, India", "company": "TechCorp Solutions", "active": true },
                   "candidate": { "id": 2, "email": "johndoe@test.com", "fullName": "John Doe", "role": "CANDIDATE" },
                   "status": "PENDING",
                   "appliedAt": "2026-05-28T21:49:36.493"
                 }
               }

  5. Verify application in dashboard: GET http://localhost:5000/api/applications/my
     Request:  ""
     Response: {
                 "success": true,
                 "message": "Applications retrieved",
                 "data": [
                   {
                     "id": 9,
                     "job": { "id": 1, "title": "Software Developer", "location": "Bangalore, India", "company": "TechCorp Solutions" },
                     "candidate": { "id": 2, "email": "johndoe@test.com", "fullName": "John Doe", "role": "CANDIDATE" },
                     "status": "PENDING",
                     "appliedAt": "2026-05-28T21:49:36.493"
                   }
                 ]
               }
```

---

## Step 6: Excel Column Format

| Column | Description | When Filled |
|---|---|---|
| `Sl No` | Sequential number | First step row only |
| `TestCaseId` | Unique ID | First step row only |
| `TestCaseName` | Short action-based name | First step row only |
| `TestCaseDescription` | What the test verifies | First step row only |
| `TestSteps` | Action + endpoint | Every row |
| `Request Body` | Prettified JSON payload or full URL with params | Every row |
| `Response Body` | Full prettified JSON response | Every row |

---

## Step 7: Coverage Categories

| Category | Min Tests |
|---|---|
| Happy Path | 1 per business action (multi-step flow) |
| Business Negative | 1 per business rule (e.g. duplicate apply → 409, invalid jobId → 404) |
| Role-based | When story mentions roles |
| E2E | 1 complete multi-step flow per user story |

---

## Step 8: Naming Conventions

| Item | Convention | Example |
|---|---|---|
| JSON file | Exact match to user story name | `HiringManagementSystem.json` |
| `testCaseId` | `TC_` + story prefix + 3-digit sequence | `TC_HMS_001` |
| `testCaseName` | Plain action-based — **no category suffixes** | `Apply for a Job Based on Location` |
| `step` | Action label + method + full URL | `Apply for the job: POST http://localhost:5000/api/applications` |

> ❌ Do NOT use suffixes like `- Positive`, `- Negative`, `- Happy Path`, `- No Auth`, `- E2E`
> ❌ Do NOT put just "200 OK" or "201 Created" as the full response — always include the JSON body

---

## Output Summary

| File | Location | Created By | When |
|---|---|---|---|
| `<UserStoryName>.json` | `src/main/resources/TestCases/data/` | **This skill — you create this** | Step 1: immediately after requirements analysis |
| `<UserStoryName>.xlsx` | `src/main/resources/TestCases/` | `TestCaseGenerator.generate()` — auto | Step 2: immediately after JSON is created — **before** any test class |

> ✅ Both files must exist and be verified before handing off to code-generation.

---

## Combined Rules

### Phase 1 — Requirement Understanding
- ✅ Always read the user-provided user story first.
- ✅ Always reference POJO classes for payload fields — never guess field names.
- ✅ Only generate test cases **directly described in or derived from the user story**.
- ✅ Always include at least **ONE E2E test case** as the last test case.
- ✅ Mode 1 (high-level / one test case) → generate **1 E2E test case only**.
- ✅ Mode 2 (all scenarios / complex) → **one per AC + business negatives + 1 E2E** at the end.
- ✅ Identify API dependencies (e.g., need jobId from search before applying).
- ✅ **Use generic role-based names** in all test case content — NEVER use real person names.
- ❌ Do NOT generate: login with invalid password, register with duplicate email, missing auth token, missing mandatory fields.
- ❌ Do NOT generate test cases for features NOT mentioned in the user story.
- ❌ **NEVER generate test cases for Admin deleting a Candidate** (`DELETE /api/admin/users/{id}`) — Admin deleting any user (Candidate, Recruiter, etc.) is excluded from all test case generation regardless of what any user story section says.
- ❌ NEVER use real names (Manik, Prajwal, Omkar, Jaydeep, Smitha) in `fullName`, `coverLetter`, or any test step content.

### Phase 2 — Test Case Generation
- ✅ **Always ensure `src/main/resources/TestCases/data/` directory exists** — create it if missing.
- ✅ Create a **new JSON file** for every new user story — never reuse another story's JSON.
- ✅ JSON file name **must exactly match** the user story name.
- ✅ Every AC from Phase 1 must have at least one test case in the JSON.
- ✅ Steps must include actual API endpoint URLs (`http://localhost:5000/...`).
- ✅ Request and Response bodies must be **prettified JSON** — not one-liner strings.
- ✅ Response bodies must reflect **realistic nested API output** (job object, candidate object, status etc.).
- ✅ Each test case should cover a **complete business flow** (multi-step).
- ✅ Use real field names from POJO classes in `src/main/java/com/hiring/pojo/`.
- ✅ **Run `TestCaseGenerator` IMMEDIATELY after creating the JSON** — Excel must exist before any test class is written.
- ✅ **Verify Excel row count** matches step count in JSON before proceeding to code generation.
- ❌ NEVER write a test class before the JSON AND Excel are both created and verified.
- ❌ NEVER put just "200 OK" or "201 Created" as the full response — always include the JSON body.
- ❌ NEVER put `TestCaseEntry` objects in `SampleTest.java` or any test file.
- ❌ NEVER modify `TestCaseGenerator.java` for new user stories — just create the JSON.
- ❌ NEVER hardcode row numbers in `updateExcelData` — rows are looked up by `TestCaseId`.
- ❌ NEVER duplicate `testCaseId` values.
