---
name: code-generation
description: "Framework patterns, test class template, entity/helper mapping, test method naming conventions, helper method signatures (ActorHelper, CommonMethod, BaseTest, RestUtils, URLGenerator), POJO construction pattern, test pattern descriptions, complete code examples. Use during code generation."
user-invocable: false
---

# Code Generation Guidelines — QA Hiring Management System API Tests

All generated Java code MUST follow the Critical Framework Patterns and naming conventions in this skill.

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
│   │       ├── BaseTest.java             # Base class: role-based token setup, tearDown
│   │       ├── RestUtils.java            # REST Assured wrapper: GET/POST/PUT/DELETE/PATCH/upload
│   │       └── URLGenerator.java        # All API endpoint constants
│   └── resources/
│       ├── TestCases/
│       │   ├── data/
│       │   │   └── <UserStoryName>.json  # Input: test case definitions (schema below)
│       │   └── <UserStoryName>.xlsx      # Output: generated Excel test case file
│       ├── UserStory/
│       │   └── <UserStoryName>.doc       # User story Word documents
│       └── testdata/
│           ├── config.properties         # base.url, admin/recruiter/candidate credentials
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

```java
package com.hiring.tests;

import com.hiring.commonMethods.CommonMethod;
import com.hiring.helpers.ActorHelper;
import com.hiring.utils.BaseTest;
import com.hiring.utils.RestUtils;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

public class <ClassName> extends BaseTest {

    public BaseTest baseTest;

    // Role-specific ActorHelper instances
    public ActorHelper actorHelperForAdmin;
    public ActorHelper actorHelperForRecruiter;
    public ActorHelper actorHelperForCandidate;

    // Role-specific RestUtils instances
    public RestUtils restUtilsForAdmin;
    public RestUtils restUtilsForRecruiter;
    public RestUtils restUtilsForCandidate;

    // Role-specific access tokens
    public String accessTokenAdmin;
    public String accessTokenRecruiter;
    public String accessTokenCandidate;

    @BeforeClass
    public void setUp() {
        baseTest = new BaseTest();
        accessTokenAdmin    = baseTest.setUpWithRole("ADMIN");
        accessTokenRecruiter = baseTest.setUpWithRole("RECRUITER");
        accessTokenCandidate = baseTest.setUpWithRole("CANDIDATE");

        restUtilsForAdmin    = new RestUtils(accessTokenAdmin);
        actorHelperForAdmin  = new ActorHelper(restUtilsForAdmin);

        restUtilsForRecruiter    = new RestUtils(accessTokenRecruiter);
        actorHelperForRecruiter  = new ActorHelper(restUtilsForRecruiter);

        restUtilsForCandidate    = new RestUtils(accessTokenCandidate);
        actorHelperForCandidate  = new ActorHelper(restUtilsForCandidate);
    }

    // @Test methods go here
}
```

---

## 6 Critical Framework Patterns (MUST FOLLOW)

### Pattern 1 — @BeforeClass Setup

Every test class MUST extend `BaseTest` and set up role-specific `RestUtils` + `ActorHelper` instances:

```java
@BeforeClass
public void setUp() {
    baseTest = new BaseTest();
    accessTokenAdmin     = baseTest.setUpWithRole("ADMIN");
    accessTokenRecruiter = baseTest.setUpWithRole("RECRUITER");
    accessTokenCandidate = baseTest.setUpWithRole("CANDIDATE");

    restUtilsForAdmin    = new RestUtils(accessTokenAdmin);
    actorHelperForAdmin  = new ActorHelper(restUtilsForAdmin);

    restUtilsForRecruiter   = new RestUtils(accessTokenRecruiter);
    actorHelperForRecruiter = new ActorHelper(restUtilsForRecruiter);

    restUtilsForCandidate   = new RestUtils(accessTokenCandidate);
    actorHelperForCandidate = new ActorHelper(restUtilsForCandidate);
}
```

Only initialize the role-specific instances needed by the test class. If a test only uses the recruiter role, only the recruiter block is required.

### Pattern 2 — Instance Variables

```java
public BaseTest baseTest;

public ActorHelper actorHelperForAdmin;
public ActorHelper actorHelperForRecruiter;
public ActorHelper actorHelperForCandidate;

public RestUtils restUtilsForAdmin;
public RestUtils restUtilsForRecruiter;
public RestUtils restUtilsForCandidate;

public String accessTokenAdmin;
public String accessTokenRecruiter;
public String accessTokenCandidate;
```

### Pattern 3 — Test Data Reading

Use `CommonMethod.readTestData(filePath)` to load JSON files from `src/main/resources/testdata/`:

```java
HashMap<String, String> testData = CommonMethod.readTestData("src/main/resources/testdata/create-job.json");
```

Each JSON file is a **flat key-value map** where all values are strings.

> ⚠️ **File naming rule:** The JSON file name should describe the scenario (e.g., `create-job.json`, `apply-job.json`, `register-candidate.json`).

### Pattern 4 — Test Data JSON Format

All test data files are flat JSON with string values only:

```json
{
  "title": "Software Engineer",
  "description": "Develop and maintain Java applications",
  "location": "Pune",
  "company": "ABC Pvt Ltd",
  "salary": "10 LPA",
  "type": "FULL_TIME"
}
```

Numeric values that need to be passed as integers in POJOs are stored as strings in JSON and converted in the POJO builder using `Integer.parseInt(testData.get("fieldName"))`.

### Pattern 5 — Test Method Structure

```java
/**
 * Brief description of what the test validates.
 * Steps:
 * 1. Read test data from JSON
 * 2. Call ActorHelper method with the correct role
 * 3. Assert response status code and body fields
 */
@Test(groups = {"Smoke"}, description = "testMethodName")
public void testMethodName() throws Exception {
    HashMap<String, String> testData = CommonMethod.readTestData("src/main/resources/testdata/<file>.json");
    Response response = actorHelperFor<Role>.<helperMethod>(testData);
    Assert.assertEquals(response.getStatusCode(), <expectedCode>, "<assertion message>");
}
```

> ⚠️ **CRITICAL — Test Group Rule:**
> Use `groups = {"Smoke"}` for basic happy-path tests and `groups = {"Regression"}` for negative/edge-case tests.
> Always include `description` matching the method name.

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
| Role-based token setup | `BaseTest` | `setUpWithRole(role)` |

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
// Valid status values: PENDING, REVIEWED, SHORTLISTED, REJECTED, ACCEPTED
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
URLGenerator.APPLICATION_STATUS        // PUT   /api/applications/{id}/status

// User Profile
URLGenerator.USER_PROFILE              // GET, PUT  /api/users/profile

// File Upload
URLGenerator.UPLOAD_RESUME             // POST  /api/upload/resume

// Admin
URLGenerator.ADMIN_USERS               // GET   /api/admin/users
URLGenerator.ADMIN_USER_BY_ID          // DELETE /api/admin/users/{id}
```

---

## BaseTest — Key Methods

```java
// Set up and retrieve a role-based token.
// role: "ADMIN" | "RECRUITER" | "CANDIDATE"
public String setUpWithRole(String role)

// Static token generators (called by setUpWithRole internally)
public static String getAdminToken()
public static String getRecruiterToken()
public static String getCandidateToken()

// Generic token generator (use when email/password are known)
public static String generateAccessToken(String email, String password)
```

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

### Pattern 1 — Happy Path (Role-specific action)
Read test data → call ActorHelper with the correct role → assert 200/201 status → optionally assert response body field.

```java
@Test(groups = {"Smoke"}, description = "postNewJob")
public void postNewJob() throws Exception {
    HashMap<String, String> testData = CommonMethod.readTestData("src/main/resources/testdata/create-job.json");
    Response response = actorHelperForRecruiter.createJob(testData);
    Assert.assertEquals(response.getStatusCode(), 200, "Job creation failed");
}
```

### Pattern 2 — Chain: Create then Read
Create a resource → extract ID from response → use ID in subsequent call → assert result.

```java
@Test(groups = {"Regression"}, description = "createAndGetJobById")
public void createAndGetJobById() throws Exception {
    HashMap<String, String> testData = CommonMethod.readTestData("src/main/resources/testdata/create-job.json");
    Response createResponse = actorHelperForRecruiter.createJob(testData);
    Assert.assertEquals(createResponse.getStatusCode(), 200);
    String jobId = createResponse.jsonPath().getString("data.id");

    Response getResponse = actorHelperForRecruiter.getJobById(jobId);
    Assert.assertEquals(getResponse.getStatusCode(), 200);
    Assert.assertEquals(getResponse.jsonPath().getString("data.title"), testData.get("title"));
}
```

### Pattern 3 — Chain: Create → Act → Validate
Create a job → apply as candidate → recruiter updates status → validate status change.

```java
@Test(groups = {"Regression"}, description = "applyAndUpdateApplicationStatus")
public void applyAndUpdateApplicationStatus() throws Exception {
    // Step 1: Recruiter creates job
    HashMap<String, String> jobData = CommonMethod.readTestData("src/main/resources/testdata/create-job.json");
    Response jobResponse = actorHelperForRecruiter.createJob(jobData);
    String jobId = jobResponse.jsonPath().getString("data.id");

    // Step 2: Candidate applies
    HashMap<String, String> applyData = CommonMethod.readTestData("src/main/resources/testdata/apply-job.json");
    applyData.put("jobId", jobId);
    Response applyResponse = actorHelperForCandidate.applyForJob(applyData);
    String applicationId = applyResponse.jsonPath().getString("data.id");

    // Step 3: Recruiter shortlists
    Response statusResponse = actorHelperForRecruiter.updateApplicationStatus(applicationId, "SHORTLISTED");
    Assert.assertEquals(statusResponse.getStatusCode(), 200);
}
```

### Pattern 4 — Negative / Authorization Test
Attempt an action with the wrong role → expect 401 or 403.

```java
@Test(groups = {"Regression"}, description = "verifyCandidateCannotCreateJob")
public void verifyCandidateCannotCreateJob() throws Exception {
    HashMap<String, String> testData = CommonMethod.readTestData("src/main/resources/testdata/create-job.json");
    Response response = actorHelperForCandidate.createJob(testData);
    Assert.assertEquals(response.getStatusCode(), 403, "Candidate should not be able to create a job");
}
```

### Pattern 5 — Profile Update and Validation
Update profile → get profile → assert updated fields match.

```java
@Test(groups = {"Regression"}, description = "updateAndVerifyUserProfile")
public void updateAndVerifyUserProfile() throws Exception {
    HashMap<String, String> testData = CommonMethod.readTestData("src/main/resources/testdata/update-profile.json");
    Response updateResponse = actorHelperForCandidate.updateUserProfile(testData);
    Assert.assertEquals(updateResponse.getStatusCode(), 200);

    Response getResponse = actorHelperForCandidate.getUserProfile();
    Assert.assertEquals(getResponse.jsonPath().getString("data.fullName"), testData.get("fullName"));
}
```

### Pattern 6 — Admin Operations
Admin lists users → verifies target user exists → deletes user → verifies deletion.

```java
@Test(groups = {"Regression"}, description = "adminDeleteUser")
public void adminDeleteUser() throws Exception {
    Response listResponse = actorHelperForAdmin.getAllUsers();
    Assert.assertEquals(listResponse.getStatusCode(), 200);
    // Extract a user ID and delete
    String userId = listResponse.jsonPath().getString("data[0].id");

    Response deleteResponse = actorHelperForAdmin.deleteUser(userId);
    Assert.assertEquals(deleteResponse.getStatusCode(), 200);
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
| `config.properties` | Environment config | `base.url`, `admin.email`, `admin.password`, `recruiter.email`, `recruiter.password`, `candidate.email`, `candidate.password` |

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
