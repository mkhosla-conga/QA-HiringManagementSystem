---
name: code-generation
description: "Framework patterns, test class template, entity/helper mapping, test method naming conventions, helper method signatures (ActorHelper, CommonMethod, BaseTest, RestUtils, URLGenerator), POJO construction pattern, test pattern descriptions, complete code examples. Use during code generation."
user-invocable: false
---

# Code Generation Guidelines — QA Hiring Management System API Tests

All generated Java code MUST follow the Critical Framework Patterns and naming conventions in this skill.

---

## ⛔ PREREQUISITE — MANDATORY before generating any test class

> **Do NOT start code generation until BOTH of these files exist:**
>
> | File | Path | Must exist? |
> |---|---|---|
> | `<UserStoryName>.json` | `src/main/resources/TestCases/data/<UserStoryName>.json` | ✅ YES |
> | `<UserStoryName>.xlsx` | `src/main/resources/TestCases/<UserStoryName>.xlsx` | ✅ YES |
>
> **Correct pipeline order:**
> ```
> 1. Create <UserStoryName>.json          ← testcase-generation skill
> 2. Run TestCaseGenerator → .xlsx        ← testcase-generation skill (immediately after step 1)
> 3. Write <ClassName>Test.java           ← this skill (only after steps 1+2 are done)
> ```
>
> ❌ NEVER skip step 2. Excel MUST be generated right after JSON, before the test class.

---

## ⚠️ GENERIC NAMING RULE — MANDATORY in all test class code

> **NEVER use real person names** anywhere in test code — not in comments, not in log messages, not in assert messages, not in string literals.  
> Always use **generic role-based identifiers**: `Admin1`, `Recruiter1`, `Recruiter2`, `Candidate1`, `Candidate2`, `Candidate3`.

| Real name (NEVER use) | Generic replacement (ALWAYS use) |
|---|---|
| `manik`, `Manik` | `Candidate1` |
| `prajwal`, `Prajwal` | `Candidate2` |
| `omkar`, `Omkar` | `Candidate3` |
| `jaydeep`, `Jaydeep` | `Recruiter1` |
| `smitha`, `Smitha` | `Recruiter2` |
| Any real person's name in Admin role | `Admin1` |

**This rule applies EVERYWHERE in the test class:**
- All `HashMap.put("coverLetter", ...)` values
- All `log.info(...)` step labels and messages
- All `Assert` failure messages
- All inline `//` comments and Javadoc
- All class-level actor mapping comments

**Correct:**
```java
// admin      → Admin1     (userDetails prefix: admin)
// recruiter1 → Recruiter1 (userDetails prefix: recruiter1)
// candidate1 → Candidate1 (userDetails prefix: candidate1)

public ActorHelper actorHelperForCandidate1;   // Candidate1 — candidate1 prefix

log.info("[E2E] ━━━ STEP 22 ━━━ Admin1 deletes Candidate3");
Assert.assertNotNull(userId, "Candidate3 must exist before deletion");
```

**Wrong:**
```java
// candidate3  → manik@test.com   (Candidate1 in story)   ← ❌ real name in comment
public ActorHelper actorHelperForCandidate1;   // manik — candidate3 prefix  ← ❌ real name
log.info("[E2E] ━━━ STEP 22 ━━━ Admin1 deletes Candidate3 (omkar@test.com)");  ← ❌ real name in log
Assert.assertNotNull(userId, "Candidate3 (omkar) must exist");  ← ❌ real name in assert message
```

> ✅ Email addresses are fetched at runtime via `tokenManager.getEmail("prefix")` — they are NEVER hardcoded as string literals in test code.  
> ✅ `userDetails.properties` prefix keys (`"candidate1"`, `"candidate2"`, `"candidate3"`, etc.) are technical keys, NOT names — they are acceptable.

---

## Framework Directory Structure

```
src/
├── main/
│   ├── java/com/hiring/
│   │   ├── commonMethods/
│   │   │   └── CommonMethod.java         # readTestData(), Excel CRUD (createExcelAndAddData,
│   │   │                                 #   readExcelData, updateExcelData), TestCaseEntry inner class
│   │   ├── generator/
│   │   │   └── TestCaseGenerator.java    # Generates .xlsx test case files from JSON data files.
│   │   │                                 #   Reads: TestCases/data/<Story>.json
│   │   │                                 #   Writes: TestCases/<Story>.xlsx
│   │   ├── helpers/
│   │   │   └── ActorHelper.java          # Orchestration hub: builds payloads via POJOs → calls RestUtils → returns Response
│   │   ├── pojo/                         # Request payload POJOs
│   │   │   ├── ApplicationRequestPOJO.java   # POST /api/applications
│   │   │   ├── CandidatePOJO.java            # Candidate entity
│   │   │   ├── JobRequestPOJO.java           # POST/PUT /api/jobs
│   │   │   ├── LoginRequestPOJO.java         # POST /api/auth/login
│   │   │   ├── RegisterRequestPOJO.java      # POST /api/auth/register
│   │   │   └── UserProfileRequestPOJO.java   # PUT /api/users/profile
│   │   ├── response/                     # Response deserialisation classes
│   │   │   ├── ApplicationResponse.java  # Application API response
│   │   │   └── UserProfileResponse.java  # User Profile API response
│   │   └── utils/
│   │       ├── BaseTest.java             # Base class: tearDown, generateAccessToken
│   │       ├── RestUtils.java            # REST Assured wrapper: GET/POST/PUT/DELETE/PATCH/upload
│   │       ├── URLGenerator.java         # All API endpoint constants
│   │       └── UserTokenManager.java     # ⭐ Reads userDetails.properties → generates all JWT tokens → stores in HashMap
│   └── resources/
│       ├── TestCases/
│       │   ├── data/
│       │   │   └── <UserStoryName>.json  # Input: test case definitions (schema below)
│       │   └── <UserStoryName>.xlsx      # Output: generated Excel test case file
│       ├── UserStory/
│       │   └── <UserStoryName>.doc       # User story Word documents
│       └── testdata/
│           ├── config.properties         # base.url only
│           ├── userDetails.properties    # ⭐ ALL user credentials (email + password per user prefix)
│           ├── apply-job.json            # Test data for apply-job tests
│           ├── candidate.json            # Test data for candidate tests
│           ├── create-job.json           # Test data for create-job tests
│           ├── register-candidate.json   # Test data for register-candidate tests
│           ├── register-recruiter.json   # Test data for register-recruiter tests
│           └── update-profile.json       # Test data for update-profile tests
└── test/
    └── java/com/hiring/
        └── tests/
            └── SampleTest.java           # Example test class
```

---

## Test Class Template

The generated test class MUST have:
- **ONE `@BeforeClass setUp()`** — loads all tokens via `UserTokenManager`
- **ONE `@Test` method** — all test steps from the JSON test cases are written **sequentially inline**
- **NO** `dependsOnMethods`, NO per-step `@Test` methods, NO private helper methods

> ✅ This ensures: if any step fails → the entire test stops immediately (correct E2E behaviour).  
> ✅ All IDs captured in one step (e.g. `jobId`, `appId`) are **local variables** passed naturally to the next step.  
> ❌ NEVER split steps into separate `@Test` methods.

```java
package com.hiring.tests;

import com.hiring.commonMethods.CommonMethod;
import com.hiring.helpers.ActorHelper;
import com.hiring.pojo.ApplicationRequestPOJO;
import com.hiring.utils.BaseTest;
import com.hiring.utils.RestUtils;
import com.hiring.utils.URLGenerator;
import com.hiring.utils.UserTokenManager;
import com.google.gson.Gson;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <UserStoryName> — End-to-End Test (Single @Test, all steps inlined)
 *
 * Steps:
 *  Step 01 – ...
 *  Step 02 – ...
 *  (one line per test case step from the JSON test case file)
 */
public class <ClassName> extends BaseTest {

    private static final Logger log = LogManager.getLogger(<ClassName>.class);

    // ── UserTokenManager — single source of truth for all credentials ──────────
    private UserTokenManager tokenManager;

    // ── Actors: only declare what the test actually uses ──────────────────────
    public ActorHelper actorHelperForAdmin;
    public ActorHelper actorHelperForRecruiter;
    public ActorHelper actorHelperForCandidate;

    public RestUtils restUtilsForAdmin;
    public RestUtils restUtilsForRecruiter;
    public RestUtils restUtilsForCandidate;

    // Access tokens — for reference / debugging only
    public String accessTokenAdmin;
    public String accessTokenRecruiter;
    public String accessTokenCandidate;

    private final Gson gson = new Gson();

    // ═══════════════════════════════════════════════════════════════════════════
    //  SETUP — runs once before the @Test method
    // ═══════════════════════════════════════════════════════════════════════════

    @BeforeClass
    public void setUp() {
        /*
         * UserTokenManager reads userDetails.properties and generates all tokens.
         * NEVER hardcode email or password here — always use tokenManager.getToken/getEmail/getPassword.
         */
        tokenManager = new UserTokenManager();

        accessTokenAdmin     = tokenManager.getToken("admin");
        accessTokenRecruiter = tokenManager.getToken("recruiter1");
        accessTokenCandidate = tokenManager.getToken("candidate1");

        restUtilsForAdmin    = tokenManager.getRestUtils("admin");
        actorHelperForAdmin  = tokenManager.getActorHelper("admin");

        restUtilsForRecruiter   = tokenManager.getRestUtils("recruiter1");
        actorHelperForRecruiter = tokenManager.getActorHelper("recruiter1");

        restUtilsForCandidate   = tokenManager.getRestUtils("candidate1");
        actorHelperForCandidate = tokenManager.getActorHelper("candidate1");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  SINGLE @Test — ALL steps from the JSON test case file inlined sequentially
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * TC_XXX_001 — <TestCaseName>
     * <TestCaseDescription>
     */
    @Test(groups = {"Regression"}, description = "TC_XXX_001 - <TestCaseName>")
    public void <testMethodName>() throws Exception {

        // ── Load per-test static data — MANDATORY, always first line ─────────
        // File name = test method name, e.g. <testMethodName>.json
        HashMap<String, String> testData = CommonMethod.readTestData(
                "src/main/resources/testdata/<testMethodName>.json");

        // ── STEP 01 — <Step description from JSON> ───────────────────────────
        log.info("[E2E] ━━━ STEP 01 ━━━ <step label>");
        // ... populate HashMap using testData.get("key"), then call ActorHelper ...

        // ── STEP 02 — <Step description from JSON> ───────────────────────────
        log.info("[E2E] ━━━ STEP 02 ━━━ <step label>");
        // ... API call + Assert ...

        // (continue for all steps in the JSON test case)

        log.info("[E2E] ━━━ ALL STEPS COMPLETED SUCCESSFULLY ━━━");
    }
}
```

---

## 6 Critical Framework Patterns (MUST FOLLOW)

### Pattern 1 — @BeforeClass Setup

Every test class MUST extend `BaseTest` and use `UserTokenManager` to load all JWT tokens from `userDetails.properties`.  
**NEVER hardcode email or password in a test class.**

```java
@BeforeClass
public void setUp() {
    tokenManager = new UserTokenManager();

    // Read tokens by user prefix (defined in userDetails.properties)
    accessTokenAdmin     = tokenManager.getToken("admin");
    accessTokenRecruiter = tokenManager.getToken("recruiter1");
    accessTokenCandidate = tokenManager.getToken("candidate1");

    restUtilsForAdmin    = tokenManager.getRestUtils("admin");
    actorHelperForAdmin  = tokenManager.getActorHelper("admin");

    restUtilsForRecruiter   = tokenManager.getRestUtils("recruiter1");
    actorHelperForRecruiter = tokenManager.getActorHelper("recruiter1");

    restUtilsForCandidate   = tokenManager.getRestUtils("candidate1");
    actorHelperForCandidate = tokenManager.getActorHelper("candidate1");
}
```

Only initialise the actors needed by the test class. If a test only uses the recruiter role, only the recruiter block is needed.

### Pattern 2 — Instance Variables

```java
// Single source of truth for all credentials — NEVER hardcode email/password
private UserTokenManager tokenManager;

public ActorHelper actorHelperForAdmin;
public ActorHelper actorHelperForRecruiter;
public ActorHelper actorHelperForCandidate;

public RestUtils restUtilsForAdmin;
public RestUtils restUtilsForRecruiter;
public RestUtils restUtilsForCandidate;

// Stored for reference / debugging only
public String accessTokenAdmin;
public String accessTokenRecruiter;
public String accessTokenCandidate;
```

### Pattern 3 — Test Data Reading

Use `CommonMethod.readTestData(filePath)` to load JSON files from `src/main/resources/testdata/`.

> ✅ **Per-Test Data File Rule (MANDATORY for multi-actor / multi-step tests):**  
> Every `@Test` method MUST have its own dedicated JSON file named after the test method.  
> **File name = test method name**, e.g. `fullHiringLifecycleE2E.json` for `fullHiringLifecycleE2E()`.  
> This keeps each test's static data isolated — changes to one test never affect another.

```java
// Load at the very start of the @Test method — one load, used throughout
HashMap<String, String> testData = CommonMethod.readTestData(
        "src/main/resources/testdata/fullHiringLifecycleE2E.json");
```

> ❌ NEVER hardcode fullName, phone, role, job titles, descriptions, cover letters, or any other  
> static test data as string literals inside the test class.  
> ✅ Always read them from the per-test JSON file via `testData.get("key")`.

### Pattern 4 — Per-Test Data JSON Key Convention

> ## 🔴 MANDATORY — Read `userDetails.properties` Before Creating Testdata JSON
>
> **Before writing any testdata JSON file**, read `src/main/resources/testdata/userDetails.properties`
> to get the **exact user prefixes** and their **actual email addresses**.  
> The key prefixes in your testdata JSON MUST match the prefixes in `userDetails.properties` exactly.
>
> ### Actual User Registry (from `userDetails.properties`)
>
> | Prefix | Actual Email | Password | Role | Generic fullName to use in JSON |
> |---|---|---|---|---|
> | `admin` | `admin@test.com` | `admin123` | ADMIN | `Admin1` |
> | `recruiter1` | `jaydeep@test.com` | `pass123` | RECRUITER | `Recruiter1` |
> | `recruiter2` | `smitha@test.com` | `pass123` | RECRUITER | `Recruiter2` |
> | `candidate1` | `omkar@test.com` | `pass123` | CANDIDATE | `Candidate1` |
> | `candidate2` | `prajwal@test.com` | `pass123` | CANDIDATE | `Candidate2` |
> | `candidate3` | `manik@test.com` | `pass123` | CANDIDATE | `Candidate3` |
>
> ✅ **Email and password are NEVER put in the testdata JSON** — they are read at runtime via  
> `tokenManager.getEmail("prefix")` / `tokenManager.getPassword("prefix")`.  
> ✅ The `fullName` in testdata JSON uses the **generic role-based identifier** (`Candidate1`, `Recruiter1`, etc.) — never real names.  
> ✅ The prefix used in testdata JSON keys (e.g. `candidate1.fullName`) MUST match the prefix in `userDetails.properties` (e.g. `candidate1.email`).  
> ❌ Do NOT invent new prefixes — only use: `admin`, `recruiter1`, `recruiter2`, `candidate1`, `candidate2`, `candidate3`.

All per-test data files use **prefixed flat keys** so all data for a test lives in one file without collisions:

```
<prefix>.<field>      — actor registration data  (prefix MUST match userDetails.properties prefix)
<jobN>.<field>        — job creation data  
<appKey>.coverLetter  — application cover letter
```

**Full example — `fullHiringLifecycleE2E.json`:**

> Key prefixes (`admin`, `recruiter1`, `candidate1`) match `userDetails.properties` exactly.  
> `fullName` values use generic identifiers — NOT real names from email addresses.

```json
{
  "admin.fullName": "Admin1",
  "admin.phone":    "9000000001",
  "admin.role":     "ADMIN",

  "recruiter1.fullName": "Recruiter1",
  "recruiter1.phone":    "9000000002",
  "recruiter1.role":     "RECRUITER",

  "recruiter2.fullName": "Recruiter2",
  "recruiter2.phone":    "9000000003",
  "recruiter2.role":     "RECRUITER",

  "candidate1.fullName": "Candidate1",
  "candidate1.phone":    "9000000004",
  "candidate1.role":     "CANDIDATE",

  "candidate2.fullName": "Candidate2",
  "candidate2.phone":    "9000000005",
  "candidate2.role":     "CANDIDATE",

  "candidate3.fullName": "Candidate3",
  "candidate3.phone":    "9000000006",
  "candidate3.role":     "CANDIDATE",

  "job1.title":       "Backend Engineer",
  "job1.description": "Java Spring Boot developer",
  "job1.location":    "Bangalore, India",
  "job1.company":     "CloudNine Technologies",
  "job1.salary":      "12,00,000 - 18,00,000",
  "job1.type":        "FULL_TIME",

  "c1Job1.coverLetter": "Candidate1 applying for Backend Engineer at CloudNine.",
  "dup.coverLetter":    "Duplicate application attempt.",
  "closedJob.coverLetter": "Attempting to apply for closed Backend Engineer role."
}
```

**Usage in test — prefix binding across files:**

```java
// testData key prefix "candidate1" → binds to userDetails.properties "candidate1.email=omkar@test.com"
HashMap<String, String> testData = CommonMethod.readTestData(
        "src/main/resources/testdata/fullHiringLifecycleE2E.json");

// Static fields from testdata JSON (prefix must match userDetails.properties)
adminReg.put("fullName", testData.get("admin.fullName"));   // "Admin1"
adminReg.put("phone",    testData.get("admin.phone"));       // "9000000001"
adminReg.put("role",     testData.get("admin.role"));        // "ADMIN"

// Runtime credentials always from tokenManager — NEVER from testdata JSON
adminReg.put("email",    tokenManager.getEmail("admin"));    // → "admin@test.com"
adminReg.put("password", tokenManager.getPassword("admin")); // → "admin123"

job1Data.put("title",    testData.get("job1.title"));

applyData.put("coverLetter", testData.get("c1Job1.coverLetter"));
```

> ⚠️ Email and password are NEVER in the per-test JSON — they always come from  
> `tokenManager.getEmail("prefix")` / `tokenManager.getPassword("prefix")` (read from `userDetails.properties`).

Numeric values that need to be passed as integers in POJOs are stored as strings in JSON and converted in the POJO builder using `Integer.parseInt(testData.get("fieldName"))`.


### Pattern 5 — Single @Test Method Structure (MANDATORY)

> ✅ **Every user story generates exactly ONE `@Test` method.**  
> All steps from the JSON test case are written **flat and sequential** inside that single method.  
> ❌ NEVER create one `@Test` per step. ❌ NEVER use `dependsOnMethods`.

```java
/**
 * TC_XXX_001 — <TestCaseName from JSON testCaseName>
 * <TestCaseDescription from JSON testCaseDescription>
 */
@Test(groups = {"Regression"}, description = "TC_XXX_001 - <testCaseName>")
public void <testMethodName>() throws Exception {

    // ── Load per-test static data — MANDATORY, always the first line ──────
    // ❌ NEVER put static values (names, phones, titles, cover letters) directly in the test.
    // ✅ ALL static data must be in src/main/resources/testdata/<testMethodName>.json
    HashMap<String, String> testData = CommonMethod.readTestData(
            "src/main/resources/testdata/<testMethodName>.json");

    // ── STEP 01 — <step field from JSON steps[0]> ──────────────────────────
    log.info("[E2E] ━━━ STEP 01 ━━━ <step label>");
    HashMap<String, String> step01Data = new HashMap<>();
    step01Data.put("key", testData.get("step01.key"));   // ✅ always from testData
    Response step01Response = actorHelperFor<Role>.<helperMethod>(step01Data);
    Assert.assertEquals(step01Response.getStatusCode(), 200, "<assertion message>");
    String capturedId = step01Response.jsonPath().getString("data.id"); // passed to next steps as local var

    // ── STEP 02 — <step field from JSON steps[1]> ──────────────────────────
    log.info("[E2E] ━━━ STEP 02 ━━━ <step label>");
    HashMap<String, String> step02Data = new HashMap<>();
    step02Data.put("jobId",       capturedId);                         // runtime value — OK as local var
    step02Data.put("coverLetter", testData.get("step02.coverLetter")); // ✅ static text from testData
    Response step02Response = actorHelperFor<Role>.<helperMethod>(step02Data);
    Assert.assertEquals(step02Response.getStatusCode(), 200, "<assertion message>");

    // (repeat for every step in JSON steps array)

    log.info("[E2E] ━━━ ALL STEPS COMPLETED SUCCESSFULLY ━━━");
}
```

> ⚠️ **Test Group Rule:**  
> Use `groups = {"Smoke"}` for basic single-action happy-path tests.  
> Use `groups = {"Regression"}` for E2E multi-step flows.  
> Always set `description` to the `testCaseId + " - " + testCaseName` from the JSON.

### How to map JSON test case steps to code

Each `steps[]` entry in the JSON becomes one inline block inside the single `@Test` method:

| JSON field | Maps to in code |
|---|---|
| `testCaseName` | `@Test description` + method name |
| `testCaseDescription` | Javadoc comment above `@Test` |
| `steps[n].step` | `log.info("[E2E] ━━━ STEP NN ━━━ ...")` label |
| `steps[n].requestBody` | `HashMap<String, String>` populated from the JSON keys |
| `steps[n].responseBody` | `Assert.assertEquals(response.getStatusCode(), ...)` + field assertions |

### Pattern 6 — POJO Request Body (MANDATORY for structured payloads)

Every POJO exposes a `create<Entity>Payload(HashMap<String, String> testData)` builder method. Always call this builder instead of constructing the POJO field-by-field in the test class.

```java
// Inside ActorHelper — the correct pattern:
String payload = gson.toJson(new RegisterRequestPOJO().createRegisterPayload(testData));
Response response = restUtils.post(URLGenerator.AUTH_REGISTER, payload);
```

Never serialize payloads inside test classes — delegate to `ActorHelper`.

---

## Test Method Naming Convention

**Format**: `<verb><Entity><Scenario>`

| Test Scenario | Verb |
|---|---|
| Create a resource | `post`, `create` |
| Read a resource | `get`, `fetch` |
| Update a resource | `update`, `put` |
| Delete a resource | `delete` |
| Apply / submit | `apply`, `submit` |
| Register / login | `register`, `login` |
| Validate negative case | `verify<Entity>With<Condition>` |

**Examples:**
`postNewJob`, `getJobById`, `updateJobById`, `deleteJobById`, `applyForJob`, `getMyApplications`, `updateApplicationStatus`, `registerCandidate`, `loginAsRecruiter`, `getUserProfile`, `updateUserProfile`, `verifyJobCreationWithMissingTitle`

---

## Entity / Helper Mapping

| Entity / Feature | Helper Class | Method |
|---|---|---|
| Register user | `ActorHelper` | `registerUser(testData)` |
| Login user | `ActorHelper` | `loginUser(testData)` |
| Create job | `ActorHelper` | `createJob(testData)` |
| Update job | `ActorHelper` | `updateJob(jobId, testData)` |
| Get all jobs | `ActorHelper` | `getAllJobs()` |
| Get job by ID | `ActorHelper` | `getJobById(jobId)` |
| Delete job | `ActorHelper` | `deleteJob(jobId)` |
| Apply for job | `ActorHelper` | `applyForJob(testData)` |
| Get my applications | `ActorHelper` | `getMyApplications()` |
| Get applications by job | `ActorHelper` | `getApplicationsByJob(jobId)` |
| Update application status | `ActorHelper` | `updateApplicationStatus(applicationId, status)` |
| Get user profile | `ActorHelper` | `getUserProfile()` |
| Update user profile | `ActorHelper` | `updateUserProfile(testData)` |
| Upload resume | `ActorHelper` | `uploadResume(filePath)` |
| Get all users (Admin) | `ActorHelper` | `getAllUsers()` |
| Delete user (Admin) | `ActorHelper` | `deleteUser(userId)` |
| Read JSON test data | `CommonMethod` | `readTestData(filePath)` |
| Create Excel test case file | `CommonMethod` | `createExcelAndAddData(filePath, entries)` |
| Read Excel data | `CommonMethod` | `readExcelData(filePath)` |
| Update Excel cell | `CommonMethod` | `updateExcelData(filePath, rowNumber, columnName, newValue)` |
| Generate Excel from JSON | `TestCaseGenerator` | `generate(userStoryName)` |
| Get JWT token by user prefix | `UserTokenManager` | `getToken(userKey)` |
| Get RestUtils by user prefix | `UserTokenManager` | `getRestUtils(userKey)` |
| Get ActorHelper by user prefix | `UserTokenManager` | `getActorHelper(userKey)` |
| Get email by user prefix | `UserTokenManager` | `getEmail(userKey)` |
| Get password by user prefix | `UserTokenManager` | `getPassword(userKey)` |

---

## ActorHelper — Full Method Reference

### Authentication

```java
// Register a new user (POST /api/auth/register)
// Required testData keys: email, password, fullName, phone, role
public Response registerUser(HashMap<String, String> testData) throws Exception

// Login and get JWT token (POST /api/auth/login)
// Required testData keys: email, password
public Response loginUser(HashMap<String, String> testData) throws Exception
```

### Jobs

```java
// Create a new job — Recruiter role required (POST /api/jobs)
// Required testData keys: title, description, location, company, salary, type
public Response createJob(HashMap<String, String> testData) throws Exception

// Update a job by ID — Recruiter role required (PUT /api/jobs/{id})
// Required testData keys: title, description, location, company, salary, type
public Response updateJob(String jobId, HashMap<String, String> testData) throws Exception

// Get all active jobs (GET /api/jobs)
public Response getAllJobs() throws Exception

// Get job by ID (GET /api/jobs/{id})
public Response getJobById(String jobId) throws Exception

// Delete a job by ID — Recruiter role required (DELETE /api/jobs/{id})
public Response deleteJob(String jobId) throws Exception
```

### Applications

```java
// Apply for a job — Candidate role required (POST /api/applications)
// Required testData keys: jobId, coverLetter
public Response applyForJob(HashMap<String, String> testData) throws Exception

// Get my applications — Candidate role required (GET /api/applications/my)
public Response getMyApplications() throws Exception

// Get applicants for a job — Recruiter role required (GET /api/applications/job/{jobId})
public Response getApplicationsByJob(String jobId) throws Exception

// Update application status — Recruiter role required (PUT /api/applications/{id}/status?status=<STATUS>)
// Valid status values: INTERVIEW_SCHEDULED, ON_HOLD, REJECTED, SELECTED
// Cascading behaviour: when status=SELECTED → all other applicants for that job are auto-REJECTED
//                      and the job is deactivated (active=false) automatically by the API
public Response updateApplicationStatus(String applicationId, String status) throws Exception
```

### User Profile

```java
// Get current user profile (GET /api/users/profile)
public Response getUserProfile() throws Exception

// Update user profile (PUT /api/users/profile)
// Required testData keys: fullName, phone, skills, experience
public Response updateUserProfile(HashMap<String, String> testData) throws Exception
```

### File Upload

```java
// Upload resume — multipart/form-data (POST /api/upload/resume)
public Response uploadResume(String filePath) throws Exception
```

### Admin

```java
// List all users — Admin role required (GET /api/admin/users)
public Response getAllUsers() throws Exception

// Delete a user — Admin role required (DELETE /api/admin/users/{id})
public Response deleteUser(String userId) throws Exception
```

---

## RestUtils — Key Methods

```java
// Constructor with token
new RestUtils(accessToken)

// POST with body
public Response post(String url, Object body)

// POST without body
public Response post(String url)

// GET
public Response get(String url)

// GET with query parameters
public Response get(String url, Map<String, ?> queryParams)

// PUT with body
public Response put(String url, Object body)

// PUT with query parameters (no body, e.g., status updates)
public Response putWithQueryParams(String url, Map<String, ?> queryParams)

// PATCH with body
public Response patch(String url, Object body)

// DELETE
public Response delete(String url)

// Multipart file upload
public Response uploadFile(String url, String filePath, String fieldName)

// GET with path parameters
public Response getWithPathParams(String url, Map<String, ?> pathParams)

// Static helper — replaces {paramName} placeholder in URL
// Example: replacePathParam("/api/jobs/{id}", "id", "5") → "/api/jobs/5"
public static String replacePathParam(String url, String paramName, String value)
```

---

## URLGenerator — Endpoint Constants

```java
// Authentication
URLGenerator.AUTH_REGISTER              // POST  /api/auth/register
URLGenerator.AUTH_LOGIN                 // POST  /api/auth/login

// Jobs
URLGenerator.JOBS                       // GET (all), POST (create)  /api/jobs
URLGenerator.JOB_BY_ID                  // GET, PUT, DELETE  /api/jobs/{id}

// Applications
URLGenerator.APPLICATIONS              // POST  /api/applications
URLGenerator.MY_APPLICATIONS           // GET   /api/applications/my
URLGenerator.APPLICATIONS_BY_JOB       // GET   /api/applications/job/{jobId}
URLGenerator.APPLICATION_STATUS        // PUT   /api/applications/{id}/status  (query param: status)

// User Profile
URLGenerator.USER_PROFILE              // GET, PUT  /api/users/profile

// File Upload
URLGenerator.UPLOAD_RESUME             // POST  /api/upload/resume

// Admin
URLGenerator.ADMIN_USERS               // GET   /api/admin/users
URLGenerator.ADMIN_USER_BY_ID          // DELETE /api/admin/users/{id}
```

---

## UserTokenManager — Credential Management

`UserTokenManager` is the **single source of truth** for all user credentials and JWT tokens.  
It reads `userDetails.properties`, generates a JWT access token per user via POST `/api/auth/login`, and stores them in a `HashMap` keyed by user prefix.

> ✅ **Rule**: All test classes MUST use `UserTokenManager` to get tokens.  
> ❌ **Never** hardcode `email`, `password`, or call `setUpWithRole()` in test classes.

### `userDetails.properties` format

> ✅ **Do NOT hardcode credentials anywhere in test classes or skill examples.**  
> The actual values are defined in `src/main/resources/testdata/userDetails.properties`.  
> Always read them at runtime via `tokenManager.getEmail("prefix")` and `tokenManager.getPassword("prefix")`.

```
File: src/main/resources/testdata/userDetails.properties

Format per user:
  <prefix>.email=<value>
  <prefix>.password=<value>

Supported prefixes: admin, recruiter1, recruiter2, candidate1, candidate2, candidate3
```

### UserTokenManager API

```java
// Instantiate — loads all users and generates tokens immediately
UserTokenManager tokenManager = new UserTokenManager();

// Get JWT token for a user prefix
String token = tokenManager.getToken("recruiter1");

// Get RestUtils pre-configured with the token
RestUtils restUtils = tokenManager.getRestUtils("candidate2");

// Get ActorHelper pre-configured with the token
ActorHelper actorHelper = tokenManager.getActorHelper("admin");

// Get email for a user prefix (use when building registration payloads)
String email = tokenManager.getEmail("candidate1");

// Get password for a user prefix (use when building login/registration payloads)
String password = tokenManager.getPassword("candidate1");
```

### User Prefix → Test Actor Mapping

> 🔴 **Always read `userDetails.properties` first** to confirm the actual email per prefix before generating any testdata JSON or test class.

| Property prefix | Actual Email (from `userDetails.properties`) | Password | Role | Generic actor name | Typical field name |
|---|---|---|---|---|---|
| `admin` | `admin@test.com` | `admin123` | Admin | `Admin1` | `actorHelperForAdmin` |
| `recruiter1` | `jaydeep@test.com` | `pass123` | Recruiter | `Recruiter1` | `actorHelperForRecruiter1` |
| `recruiter2` | `smitha@test.com` | `pass123` | Recruiter | `Recruiter2` | `actorHelperForRecruiter2` |
| `candidate1` | `omkar@test.com` | `pass123` | Candidate | `Candidate1` | `actorHelperForCandidate1` |
| `candidate2` | `prajwal@test.com` | `pass123` | Candidate | `Candidate2` | `actorHelperForCandidate2` |
| `candidate3` | `manik@test.com` | `pass123` | Candidate | `Candidate3` | `actorHelperForCandidate3` |

> ⚠️ The actual email values shown above are resolved at runtime via `tokenManager.getEmail("prefix")`.  
> ⚠️ The `fullName` in testdata JSON uses the **generic name** (`Candidate1`, `Recruiter1`) — NOT the email username.  
> ✅ Testdata JSON key prefix (e.g. `candidate1.fullName`) MUST match the `userDetails.properties` prefix (`candidate1.email`).  
> ❌ Never hardcode actual email strings in test classes or testdata JSON files.

---

## BaseTest — Key Methods

```java
// Static token generator (used internally by UserTokenManager — avoid calling directly in tests)
public static String generateAccessToken(String email, String password)

// Resets RestAssured after class — called automatically via @AfterClass
public void tearDown()
```

> ⚠️ Do NOT call `setUpWithRole()` or `generateAccessToken()` directly in test classes.  
> Always use `UserTokenManager` instead.

---

## CommonMethod — Key Methods

```java
// Read a flat JSON file and return as HashMap<String, String>
// filePath: relative path from project root
// Example: "src/main/resources/testdata/create-job.json"
public static HashMap<String, String> readTestData(String filePath)

// Create (or append to) an Excel file at filePath.
// Writes each TestCaseEntry as one or more rows (one row per step).
// Skips duplicate TestCaseIds automatically.
// Excel columns: Sl No | TestCaseId | TestCaseName | TestCaseDescription | TestSteps | Request Body | Response Body
public static void createExcelAndAddData(String filePath, List<TestCaseEntry> entries) throws IOException

// Read all data rows (excluding header) from an Excel file.
// Returns each row as a Map<String, String> keyed by column header name.
public static List<Map<String, String>> readExcelData(String filePath) throws IOException

// Update a specific cell in an existing Excel file.
// rowNumber: 1-based data row index (row 1 = first data row after header)
// columnName: exact column header string, e.g. "Response Body"
public static void updateExcelData(String filePath, int rowNumber, String columnName, String newValue) throws IOException
```

### TestCaseEntry Inner Class

`CommonMethod.TestCaseEntry` is the data model used by `createExcelAndAddData`:

```java
public static class TestCaseEntry {
    public String slNo;
    public String testCaseId;
    public String testCaseName;
    public String testCaseDescription;
    public List<List<String>> steps; // each step: [stepDescription, requestBody, responseBody]

    public TestCaseEntry(String slNo, String testCaseId, String testCaseName,
                         String testCaseDescription, List<List<String>> steps)
}
```

---

## POJO Reference

### RegisterRequestPOJO

| Field | Type | Setter | testData key |
|---|---|---|---|
| `email` | `String` | `setEmail(String)` | `email` |
| `password` | `String` | `setPassword(String)` | `password` |
| `fullName` | `String` | `setFullName(String)` | `fullName` |
| `phone` | `String` | `setPhone(String)` | `phone` |
| `role` | `String` | `setRole(String)` | `role` (`CANDIDATE` / `RECRUITER`) |

Builder: `new RegisterRequestPOJO().createRegisterPayload(testData)`

---

### LoginRequestPOJO

| Field | Type | Setter | testData key |
|---|---|---|---|
| `email` | `String` | `setEmail(String)` | `email` |
| `password` | `String` | `setPassword(String)` | `password` |

Builder: `new LoginRequestPOJO().createLoginPayload(testData)`

---

### JobRequestPOJO

| Field | Type | Setter | testData key |
|---|---|---|---|
| `title` | `String` | `setTitle(String)` | `title` |
| `description` | `String` | `setDescription(String)` | `description` |
| `location` | `String` | `setLocation(String)` | `location` |
| `company` | `String` | `setCompany(String)` | `company` |
| `salary` | `String` | `setSalary(String)` | `salary` |
| `type` | `String` | `setType(String)` | `type` (`FULL_TIME` / `PART_TIME` / `CONTRACT`) |

Builder: `new JobRequestPOJO().createJobPayload(testData)`

---

### ApplicationRequestPOJO

| Field | Type | Setter | testData key |
|---|---|---|---|
| `jobId` | `int` | `setJobId(int)` | `jobId` (parsed via `Integer.parseInt`) |
| `coverLetter` | `String` | `setCoverLetter(String)` | `coverLetter` |

Builder: `new ApplicationRequestPOJO().createApplicationPayload(testData)`

---

### UserProfileRequestPOJO

| Field | Type | Setter | testData key |
|---|---|---|---|
| `fullName` | `String` | `setFullName(String)` | `fullName` |
| `phone` | `String` | `setPhone(String)` | `phone` |
| `skills` | `String` | `setSkills(String)` | `skills` |
| `experience` | `String` | `setExperience(String)` | `experience` |

Builder: `new UserProfileRequestPOJO().createUserProfilePayload(testData)`

---

### CandidatePOJO (entity, not a request builder)

| Field | Type | Setter |
|---|---|---|
| `id` | `String` | `setId(String)` |
| `firstName` | `String` | `setFirstName(String)` |
| `lastName` | `String` | `setLastName(String)` |
| `email` | `String` | `setEmail(String)` |
| `phone` | `String` | `setPhone(String)` |
| `position` | `String` | `setPosition(String)` |

---

## Response Classes

### ApplicationResponse

| Field | Type | Getter |
|---|---|---|
| `id` | `int` | `getId()` |
| `jobId` | `int` | `getJobId()` |
| `coverLetter` | `String` | `getCoverLetter()` |
| `status` | `String` | `getStatus()` |
| `applicantEmail` | `String` | `getApplicantEmail()` |

### UserProfileResponse

| Field | Type | Getter |
|---|---|---|
| `id` | `int` | `getId()` |
| `email` | `String` | `getEmail()` |
| `fullName` | `String` | `getFullName()` |
| `phone` | `String` | `getPhone()` |
| `role` | `String` | `getRole()` |
| `skills` | `String` | `getSkills()` |
| `experience` | `String` | `getExperience()` |

---

## Test Pattern Descriptions

> ✅ **All patterns below are written as a single `@Test` method with steps inlined.**  
> ❌ DO NOT write one `@Test` per step.

> ## 🔴 MANDATORY DATA ACCESS RULE — applies to ALL patterns below
>
> ❌ **NEVER hardcode** any static test value (fullName, phone, role, job title, description, location,  
> company, salary, type, cover letter, profile fields, etc.) as a string literal inside a test class.  
>
> ✅ **ALWAYS** load a per-test JSON file at the start of every `@Test` method and read all static  
> values via `testData.get("key")`:
>
> ```java
> HashMap<String, String> testData = CommonMethod.readTestData(
>         "src/main/resources/testdata/<testMethodName>.json");
> ```
>
> | Source | What goes there |
> |---|---|
> | `testdata/<testMethodName>.json` | fullName, phone, role, job fields, cover letters, profile fields |
> | `tokenManager.getEmail/getPassword("prefix")` | email, password (from `userDetails.properties`) |
> | Local variables | Runtime IDs (jobId, applicationId) captured from responses |

### Pattern 1 — Single-Step Happy Path

Simple one-step test: load per-test JSON → call ActorHelper → assert status.

```java
@Test(groups = {"Smoke"}, description = "TC_XXX_001 - Post New Job")
public void postNewJob() throws Exception {

    HashMap<String, String> testData = CommonMethod.readTestData(
            "src/main/resources/testdata/postNewJob.json");

    // ── STEP 01 — Create job ──────────────────────────────────────────────
    log.info("[E2E] ━━━ STEP 01 ━━━ Recruiter creates job");
    HashMap<String, String> jobData = new HashMap<>();
    jobData.put("title",       testData.get("job.title"));
    jobData.put("description", testData.get("job.description"));
    jobData.put("location",    testData.get("job.location"));
    jobData.put("company",     testData.get("job.company"));
    jobData.put("salary",      testData.get("job.salary"));
    jobData.put("type",        testData.get("job.type"));
    Response response = actorHelperForRecruiter.createJob(jobData);
    Assert.assertEquals(response.getStatusCode(), 200, "Job creation failed");
    log.info("[E2E] ━━━ ALL STEPS COMPLETED SUCCESSFULLY ━━━");
}
```

**`postNewJob.json`:**
```json
{
  "job.title":       "Software Developer",
  "job.description": "Java backend developer role",
  "job.location":    "Bangalore, India",
  "job.company":     "TechCorp Solutions",
  "job.salary":      "8,00,000 - 15,00,000",
  "job.type":        "FULL_TIME"
}
```

### Pattern 2 — Chain: Create then Read

Create a resource → extract ID from response → use ID in the next step.

```java
@Test(groups = {"Regression"}, description = "TC_XXX_001 - Create and Get Job By ID")
public void createAndGetJobById() throws Exception {

    HashMap<String, String> testData = CommonMethod.readTestData(
            "src/main/resources/testdata/createAndGetJobById.json");

    // ── STEP 01 — Create job ───────────────────────────────────────────────
    log.info("[E2E] ━━━ STEP 01 ━━━ Recruiter creates job");
    HashMap<String, String> jobData = new HashMap<>();
    jobData.put("title",       testData.get("job.title"));
    jobData.put("description", testData.get("job.description"));
    jobData.put("location",    testData.get("job.location"));
    jobData.put("company",     testData.get("job.company"));
    jobData.put("salary",      testData.get("job.salary"));
    jobData.put("type",        testData.get("job.type"));
    Response createResponse = actorHelperForRecruiter.createJob(jobData);
    Assert.assertEquals(createResponse.getStatusCode(), 200, "Job creation failed");
    String jobId = createResponse.jsonPath().getString("data.id");

    // ── STEP 02 — Get job by ID ────────────────────────────────────────────
    log.info("[E2E] ━━━ STEP 02 ━━━ Get job by ID");
    Response getResponse = actorHelperForRecruiter.getJobById(jobId);
    Assert.assertEquals(getResponse.getStatusCode(), 200, "Get job by ID failed");
    Assert.assertEquals(getResponse.jsonPath().getString("data.title"), testData.get("job.title"));

    log.info("[E2E] ━━━ ALL STEPS COMPLETED SUCCESSFULLY ━━━");
}
```

**`createAndGetJobById.json`:**
```json
{
  "job.title":       "Software Developer",
  "job.description": "Java backend developer role",
  "job.location":    "Bangalore, India",
  "job.company":     "TechCorp Solutions",
  "job.salary":      "8,00,000 - 15,00,000",
  "job.type":        "FULL_TIME"
}
```

### Pattern 3 — Multi-Step E2E: Create → Apply → Update Status

Full hiring flow across multiple actors — all inlined in one method.

```java
@Test(groups = {"Regression"}, description = "TC_XXX_001 - Apply and Update Application Status")
public void applyAndUpdateApplicationStatus() throws Exception {

    HashMap<String, String> testData = CommonMethod.readTestData(
            "src/main/resources/testdata/applyAndUpdateApplicationStatus.json");

    // ── STEP 01 — Recruiter creates job ───────────────────────────────────
    log.info("[E2E] ━━━ STEP 01 ━━━ Recruiter creates job");
    HashMap<String, String> jobData = new HashMap<>();
    jobData.put("title",       testData.get("job.title"));
    jobData.put("description", testData.get("job.description"));
    jobData.put("location",    testData.get("job.location"));
    jobData.put("company",     testData.get("job.company"));
    jobData.put("salary",      testData.get("job.salary"));
    jobData.put("type",        testData.get("job.type"));
    Response jobResponse = actorHelperForRecruiter.createJob(jobData);
    Assert.assertEquals(jobResponse.getStatusCode(), 200, "Job creation failed");
    String jobId = jobResponse.jsonPath().getString("data.id");

    // ── STEP 02 — Candidate applies for the job ───────────────────────────
    log.info("[E2E] ━━━ STEP 02 ━━━ Candidate applies for job");
    HashMap<String, String> applyData = new HashMap<>();
    applyData.put("jobId",       jobId);
    applyData.put("coverLetter", testData.get("apply.coverLetter"));
    Response applyResponse = actorHelperForCandidate.applyForJob(applyData);
    Assert.assertEquals(applyResponse.getStatusCode(), 200, "Application submission failed");
    String applicationId = applyResponse.jsonPath().getString("data.id");
    Assert.assertEquals(applyResponse.jsonPath().getString("data.status"), "PENDING");

    // ── STEP 03 — Recruiter shortlists the candidate ──────────────────────
    log.info("[E2E] ━━━ STEP 03 ━━━ Recruiter schedules interview for candidate");
    Response statusResponse = actorHelperForRecruiter.updateApplicationStatus(applicationId, "INTERVIEW_SCHEDULED");
    Assert.assertEquals(statusResponse.getStatusCode(), 200, "Status update failed");
    Assert.assertEquals(statusResponse.jsonPath().getString("data.status"), "INTERVIEW_SCHEDULED");
    // ⚠️ use SELECTED to trigger auto-rejection of other applicants + job deactivation

    log.info("[E2E] ━━━ ALL STEPS COMPLETED SUCCESSFULLY ━━━");
}
```

**`applyAndUpdateApplicationStatus.json`:**
```json
{
  "job.title":         "Software Developer",
  "job.description":   "Java backend developer role",
  "job.location":      "Bangalore, India",
  "job.company":       "TechCorp Solutions",
  "job.salary":        "8,00,000 - 15,00,000",
  "job.type":          "FULL_TIME",
  "apply.coverLetter": "I am excited to apply for this role."
}
```

### Pattern 4 — Negative Test (expected non-200 status)

For calls that are expected to fail (400, 403, 401), use `restUtils` directly — NOT `ActorHelper`.  
`ActorHelper` throws `Exception` on any non-200 status which would break the assertion.

> ⚠️ **Duplicate Application returns 400, NOT 409**  
> The API returns `HTTP 400 Bad Request` (not 409 Conflict) when a candidate applies to a job they have already applied for.  
> Always assert `400` for duplicate-apply negative tests.

---

### Pattern 4a — Registration Steps in E2E Tests (MANDATORY)

> 🔴 **NEVER use `actorHelper.registerUser()` for registration steps in E2E tests.**  
> `ActorHelper.registerUser()` throws `Exception` on any non-200 response.  
> In a live/shared environment, users may already exist → API returns `400 "Email already registered"` → test crashes before it starts.

**Always use `restUtils.post()` directly for ALL registration steps (Steps 01–04 pattern):**

```java
// ── STEP 01 — Register Admin1 ─────────────────────────────────────────
// ✅ Use restUtils.post() directly — tolerates 200 (new) OR 400 (already exists)
// ❌ NEVER use actorHelper.registerUser() here — it throws on non-200
HashMap<String, String> adminReg = new HashMap<>();
adminReg.put("email",    tokenManager.getEmail("admin"));
adminReg.put("password", tokenManager.getPassword("admin"));
adminReg.put("fullName", testData.get("admin.fullName"));
adminReg.put("phone",    testData.get("admin.phone"));
adminReg.put("role",     testData.get("admin.role"));
Response adminRegResp = restUtilsForAdmin.post(URLGenerator.AUTH_REGISTER,
        gson.toJson(new RegisterRequestPOJO().createRegisterPayload(adminReg)));
// 200 = newly registered; 400/409 = already exists in seeded DB — both are acceptable for setup
log.info("[E2E] Admin1 register status: {}", adminRegResp.getStatusCode());
```

**Same pattern for all actors — Recruiter1, Recruiter2, Candidate1, Candidate2, Candidate3:**

```java
Response rec1RegResp = restUtilsForRecruiter1.post(URLGenerator.AUTH_REGISTER,
        gson.toJson(new RegisterRequestPOJO().createRegisterPayload(rec1Reg)));
log.info("[E2E] Recruiter1 register status: {}", rec1RegResp.getStatusCode());
```

**Required instance variables for the `restUtils` approach:**
```java
// Declare ALL restUtils instances — needed for registration steps
public RestUtils restUtilsForAdmin;
public RestUtils restUtilsForRecruiter1;
public RestUtils restUtilsForRecruiter2;
public RestUtils restUtilsForCandidate1;
public RestUtils restUtilsForCandidate2;
public RestUtils restUtilsForCandidate3;
```

**Required import:**
```java
import com.hiring.pojo.RegisterRequestPOJO;
```

**Rule summary:**

| Step type | Use | Why |
|---|---|---|
| Registration (Steps 01–04) | `restUtils.post(URLGenerator.AUTH_REGISTER, ...)` | Tolerates 400/409 already-exists gracefully |
| All other happy-path calls | `actorHelper.<method>(...)` | Throws on non-200 → test fails fast on real failures |
| Expected-fail calls (dup apply, closed job, 401) | `restUtils.post(...)` directly | Need to assert the non-200 status code |

> ✅ **Valid recruiter-settable status values**: `INTERVIEW_SCHEDULED`, `ON_HOLD`, `REJECTED`, `SELECTED`  
> ✅ `SELECTED` is the **only** cascading trigger — auto-rejects all other applicants + deactivates the job  
> ✅ `PENDING` is the system-set initial state on application submit — recruiters do NOT set this  
> ❌ Do NOT use `REVIEWED`, `SHORTLISTED`, or `ACCEPTED` — these are not valid API status values

---

```java
@Test(groups = {"Regression"}, description = "TC_XXX_001 - Duplicate Application Blocked")
public void verifyDuplicateApplicationBlocked() throws Exception {

    HashMap<String, String> testData = CommonMethod.readTestData(
            "src/main/resources/testdata/verifyDuplicateApplicationBlocked.json");

    // ── STEP 01 — Create job and apply once ───────────────────────────────
    log.info("[E2E] ━━━ STEP 01 ━━━ Setup: create job and apply");
    HashMap<String, String> jobData = new HashMap<>();
    jobData.put("title",       testData.get("job.title"));
    jobData.put("description", testData.get("job.description"));
    jobData.put("location",    testData.get("job.location"));
    jobData.put("company",     testData.get("job.company"));
    jobData.put("salary",      testData.get("job.salary"));
    jobData.put("type",        testData.get("job.type"));
    Response jobResponse = actorHelperForRecruiter.createJob(jobData);
    String jobId = jobResponse.jsonPath().getString("data.id");

    HashMap<String, String> applyData = new HashMap<>();
    applyData.put("jobId",       jobId);
    applyData.put("coverLetter", testData.get("apply.coverLetter"));
    actorHelperForCandidate.applyForJob(applyData);

    // ── STEP 02 — Duplicate apply → expect 400 ───────────────────────────
    log.info("[E2E] ━━━ STEP 02 ━━━ Duplicate apply — expects 400");
    HashMap<String, String> dupData = new HashMap<>();
    dupData.put("jobId",       jobId);
    dupData.put("coverLetter", testData.get("dup.coverLetter"));
    String payload = gson.toJson(new ApplicationRequestPOJO().createApplicationPayload(dupData));
    Response dupResponse = restUtilsForCandidate.post(URLGenerator.APPLICATIONS, payload);
    Assert.assertEquals(dupResponse.getStatusCode(), 400, "Duplicate application should return 400 Bad Request");

    log.info("[E2E] ━━━ ALL STEPS COMPLETED SUCCESSFULLY ━━━");
}
```

**`verifyDuplicateApplicationBlocked.json`:**
```json
{
  "job.title":         "Software Developer",
  "job.description":   "Java backend developer role",
  "job.location":      "Bangalore, India",
  "job.company":       "TechCorp Solutions",
  "job.salary":        "10,00,000 - 15,00,000",
  "job.type":          "FULL_TIME",
  "apply.coverLetter": "First application.",
  "dup.coverLetter":   "Duplicate attempt."
}
```

### Pattern 5 — Profile Update and Validation

```java
@Test(groups = {"Regression"}, description = "TC_XXX_001 - Update and Verify User Profile")
public void updateAndVerifyUserProfile() throws Exception {

    HashMap<String, String> testData = CommonMethod.readTestData(
            "src/main/resources/testdata/updateAndVerifyUserProfile.json");

    // ── STEP 01 — Update profile ───────────────────────────────────────────
    log.info("[E2E] ━━━ STEP 01 ━━━ Update user profile");
    HashMap<String, String> profileData = new HashMap<>();
    profileData.put("fullName",   testData.get("profile.fullName"));
    profileData.put("phone",      testData.get("profile.phone"));
    profileData.put("skills",     testData.get("profile.skills"));
    profileData.put("experience", testData.get("profile.experience"));
    Response updateResponse = actorHelperForCandidate.updateUserProfile(profileData);
    Assert.assertEquals(updateResponse.getStatusCode(), 200, "Profile update failed");

    // ── STEP 02 — Verify updated profile ──────────────────────────────────
    log.info("[E2E] ━━━ STEP 02 ━━━ Verify updated profile");
    Response getResponse = actorHelperForCandidate.getUserProfile();
    Assert.assertEquals(getResponse.getStatusCode(), 200, "Get profile failed");
    Assert.assertEquals(getResponse.jsonPath().getString("data.fullName"), testData.get("profile.fullName"));

    log.info("[E2E] ━━━ ALL STEPS COMPLETED SUCCESSFULLY ━━━");
}
```

**`updateAndVerifyUserProfile.json`:**
```json
{
  "profile.fullName":   "Candidate1 Updated",
  "profile.phone":      "9999999999",
  "profile.skills":     "Java, REST API, TestNG",
  "profile.experience": "3 years"
}
```

### Pattern 6 — Admin Operations

> ✅ Admin-only tests (lookup by email + delete + verify login) use only `tokenManager` — no per-test JSON needed  
> unless the test also registers users with static data (fullName, phone, etc.), in which case create a JSON file.

```java
@Test(groups = {"Regression"}, description = "TC_XXX_001 - Admin Delete User")
public void adminDeleteUser() throws Exception {

    // No static form data → no per-test JSON needed here.
    // Email/password always from tokenManager — never hardcoded.

    // ── STEP 01 — Admin lists all users ───────────────────────────────────
    log.info("[E2E] ━━━ STEP 01 ━━━ Admin lists users");
    Response listResponse = actorHelperForAdmin.getAllUsers();
    Assert.assertEquals(listResponse.getStatusCode(), 200, "Admin get all users failed");
    List<Map<String, Object>> users = listResponse.jsonPath().getList("data");
    String targetUserId = null;
    for (Map<String, Object> user : users) {
        if (tokenManager.getEmail("candidate1").equals(user.get("email"))) {
            targetUserId = String.valueOf(user.get("id"));
            break;
        }
    }
    Assert.assertNotNull(targetUserId, "Target user must exist");

    // ── STEP 02 — Admin deletes the user ──────────────────────────────────
    log.info("[E2E] ━━━ STEP 02 ━━━ Admin deletes user");
    Response deleteResponse = actorHelperForAdmin.deleteUser(targetUserId);
    Assert.assertEquals(deleteResponse.getStatusCode(), 200, "Admin delete user failed");

    // ── STEP 03 — Deleted user cannot login → 401 ─────────────────────────
    // Use restUtils directly — no token, expect 401
    log.info("[E2E] ━━━ STEP 03 ━━━ Deleted user cannot login");
    String email    = tokenManager.getEmail("candidate1");
    String password = tokenManager.getPassword("candidate1");
    String loginBody = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    Response loginResponse = new RestUtils().post(URLGenerator.AUTH_LOGIN, loginBody);
    Assert.assertEquals(loginResponse.getStatusCode(), 401, "Deleted user should not be able to login");

    log.info("[E2E] ━━━ ALL STEPS COMPLETED SUCCESSFULLY ━━━");
}
```

---

## JSON Test Data Files Reference

| File | Used For | Required Keys |
|---|---|---|
| `create-job.json` | Job creation/update | `title`, `description`, `location`, `company`, `salary`, `type` |
| `apply-job.json` | Apply for job | `jobId`, `coverLetter` |
| `register-candidate.json` | Register candidate | `email`, `password`, `fullName`, `phone`, `role` (= `CANDIDATE`) |
| `register-recruiter.json` | Register recruiter | `email`, `password`, `fullName`, `phone`, `role` (= `RECRUITER`) |
| `candidate.json` | Candidate entity data | `firstName`, `lastName`, `email`, `phone`, `position` |
| `update-profile.json` | Update user profile | `fullName`, `phone`, `skills`, `experience` |
| `config.properties` | Environment config | `base.url` only — no credentials |
| `userDetails.properties` | ⭐ All user credentials | `<prefix>.email`, `<prefix>.password` per user — read by `UserTokenManager` |

---

## TestCaseGenerator — Excel Generation

`TestCaseGenerator` converts a JSON test case definition file into a formatted `.xlsx` Excel file. No code changes are required when adding new user stories — just add a new JSON file.

### Usage

**From a test class (programmatic):**
```java
TestCaseGenerator.generate("HiringManagementSystem");
// Reads:  src/main/resources/TestCases/data/HiringManagementSystem.json
// Writes: src/main/resources/TestCases/HiringManagementSystem.xlsx
```

**From Maven (no test required):**
```
mvn compile exec:java -Dexec.args="HiringManagementSystem"
```

### What It Does (3 steps)

1. **`createExcelAndAddData`** — Creates the Excel and writes all test case rows from the JSON.
2. **`readExcelData`** — Reads back all written rows to verify.
3. **`updateExcelData`** — For any test case in the JSON that has `"updateResponseBody"` set, overwrites the `Response Body` cell with that value.

### TestCases JSON Schema

File location: `src/main/resources/TestCases/data/<UserStoryName>.json`

```json
[
  {
    "slNo": "1",
    "testCaseId": "TC_HMS_001",
    "testCaseName": "Short name of the test case",
    "testCaseDescription": "Full description of what is being verified",
    "updateResponseBody": "Optional — if present, overwrites the Response Body cell after initial write",
    "steps": [
      {
        "step": "Human-readable step description + endpoint, e.g. Login: POST http://localhost:5000/api/auth/login",
        "requestBody": "JSON payload string (use \\n for line breaks) or URL for GET requests",
        "responseBody": "Expected JSON response string (use \\n for line breaks)"
      }
    ]
  }
]
```

**Field rules:**

| Field | Required | Notes |
|---|---|---|
| `slNo` | ✅ | Sequential serial number string, e.g. `"1"` |
| `testCaseId` | ✅ | Unique ID, format `TC_HMS_<NNN>`. Duplicates are skipped automatically |
| `testCaseName` | ✅ | Short descriptive name |
| `testCaseDescription` | ✅ | Full description of what the test verifies |
| `steps` | ✅ | Array of steps; at least one step required |
| `steps[].step` | ✅ | Step description + method + URL |
| `steps[].requestBody` | ✅ | JSON body (use `\n` for newlines) or URL for GETs; `""` if none |
| `steps[].responseBody` | ✅ | Expected response JSON (use `\n` for newlines); `""` if none |
| `updateResponseBody` | ❌ | Optional; when set, `updateExcelData` overwrites `Response Body` for this row |

### Generated Excel Column Layout

| Column | Header | Content |
|---|---|---|
| A | `Sl No` | Serial number |
| B | `TestCaseId` | e.g. `TC_HMS_001` |
| C | `TestCaseName` | Short name |
| D | `TestCaseDescription` | Full description |
| E | `TestSteps` | Step description (one row per step) |
| F | `Request Body` | Request payload or URL |
| G | `Response Body` | Expected response |

> For test cases with multiple steps: metadata columns (A–D) are filled only on the **first step row**. Subsequent step rows leave A–D blank.

---

## Adding a New Helper Method to ActorHelper

Follow this template when adding new API coverage:

```java
/**
 * Brief description of what the method does.
 * Steps:
 * 1. Build payload using POJO builder (if body required)
 * 2. Resolve URL using URLGenerator constant + replacePathParam (if path param needed)
 * 3. Call restUtils.<method>(url, payload)
 * 4. Throw Exception on non-200 status
 * 5. Return Response
 *
 * @param testData HashMap containing required fields
 * @return Response from the API
 * @throws Exception if the operation fails
 */
public Response myNewMethod(HashMap<String, String> testData) throws Exception {
    String payload = gson.toJson(new MyPOJO().createMyPayload(testData));
    String url = RestUtils.replacePathParam(URLGenerator.MY_ENDPOINT, "id", testData.get("id"));
    Response response = restUtils.post(url, payload);
    response.prettyPrint();
    if (response.getStatusCode() != 200) {
        throw new Exception("Failure in myNewMethod. URL: " + url + " | Status: " + response.getStatusCode());
    }
    return response;
}
```

---

## Standard Test Data Field Names

`email`, `password`, `fullName`, `phone`, `role`, `title`, `description`, `location`, `company`, `salary`, `type`, `jobId`, `coverLetter`, `skills`, `experience`, `status`, `id`
