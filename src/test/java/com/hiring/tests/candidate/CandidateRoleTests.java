package com.hiring.tests.candidate;

import com.hiring.commonMethods.CommonMethod;
import com.hiring.helpers.ActorHelper;
import com.hiring.response.ApplicationResponse;
import com.hiring.utils.BaseTest;
import com.hiring.utils.RestUtils;
import com.hiring.utils.UserTokenManager;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;

/**
 * CandidateRoleTests — Validates candidate-role API interactions.
 *
 * Actors:
 *   recruiter1 → Recruiter1  (creates jobs as pre-condition)
 *   candidate1 → Candidate1  (primary actor for all 3 test cases)
 *
 * Test Cases:
 *   TC_CR_001 – getAllJobListings      — Candidate views all active job listings
 *   TC_CR_002 – applyForJob           — Candidate applies for a job
 *   TC_CR_003 – trackApplicationStatus — Candidate tracks their application status
 */
public class CandidateRoleTests extends BaseTest {

    private static final Logger log = LogManager.getLogger(CandidateRoleTests.class);

    // ── Single source of truth for all credentials ────────────────────────────
    private UserTokenManager tokenManager;

    // ── Actors used by this test class ───────────────────────────────────────
    public ActorHelper actorHelperForRecruiter1;   // Recruiter1 — creates jobs
    public ActorHelper actorHelperForCandidate1;   // Candidate1 — primary actor

    public RestUtils restUtilsForRecruiter1;
    public RestUtils restUtilsForCandidate1;

    // Access tokens — for reference / debugging only
    public String accessTokenRecruiter1;
    public String accessTokenCandidate1;

    // ═══════════════════════════════════════════════════════════════════════════
    //  SETUP — runs once before all @Test methods in this class
    // ═══════════════════════════════════════════════════════════════════════════

    @BeforeClass
    public void setUp() {
        /*
         * UserTokenManager reads userDetails.properties and generates all tokens.
         * NEVER hardcode email or password — always use tokenManager.getToken/getEmail/getPassword.
         */
        tokenManager = new UserTokenManager();

        accessTokenRecruiter1 = tokenManager.getToken("recruiter1");
        accessTokenCandidate1 = tokenManager.getToken("candidate1");

        restUtilsForRecruiter1   = tokenManager.getRestUtils("recruiter1");
        actorHelperForRecruiter1 = tokenManager.getActorHelper("recruiter1");

        restUtilsForCandidate1   = tokenManager.getRestUtils("candidate1");
        actorHelperForCandidate1 = tokenManager.getActorHelper("candidate1");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  TC_CR_001 — Candidate Views All Job Listings
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * TC_CR_001 — Candidate Views All Job Listings
     * Verifies that Candidate1 can retrieve all active job listings and that
     * each job contains the required fields: title, company, location, type, active.
     *
     * Steps:
     *  Step 01 – Recruiter1 creates a job (ensures at least one active listing)
     *  Step 02 – Candidate1 calls GET /api/jobs
     *  Step 03 – Assert: HTTP 200, data not empty, first job has all required fields
     */
    @Test(groups = {"Smoke"}, description = "TC_CR_001 - Candidate Views All Job Listings")
    public void getAllJobListings() throws Exception {

        // ── Load per-test static data ─────────────────────────────────────────
        HashMap<String, String> testData = CommonMethod.readTestData(
                "src/main/resources/testdata/getAllJobListings.json");

        // ── STEP 01 — Recruiter1 creates a job ───────────────────────────────
        log.info("[TC_CR_001] ━━━ STEP 01 ━━━ Recruiter1 creates a job listing");
        HashMap<String, String> jobData = new HashMap<>();
        jobData.put("title",       testData.get("job.title"));
        jobData.put("description", testData.get("job.description"));
        jobData.put("location",    testData.get("job.location"));
        jobData.put("company",     testData.get("job.company"));
        jobData.put("salary",      testData.get("job.salary"));
        jobData.put("type",        testData.get("job.type"));
        Response createJobResponse = actorHelperForRecruiter1.createJob(jobData);
        Assert.assertEquals(createJobResponse.getStatusCode(), 200,
                "Recruiter1 job creation should return HTTP 200");
        String createdJobId = createJobResponse.jsonPath().getString("data.id");
        Assert.assertNotNull(createdJobId, "Created job ID must not be null");
        log.info("[TC_CR_001] Recruiter1 created job ID: {}", createdJobId);

        // ── STEP 02 — Candidate1 retrieves all active job listings ────────────
        log.info("[TC_CR_001] ━━━ STEP 02 ━━━ Candidate1 calls GET /api/jobs");
        Response getAllJobsResponse = actorHelperForCandidate1.getAllJobs();

        // ── STEP 03 — Assertions ──────────────────────────────────────────────
        log.info("[TC_CR_001] ━━━ STEP 03 ━━━ Validating job listing response");

        // Assert HTTP 200
        Assert.assertEquals(getAllJobsResponse.getStatusCode(), 200,
                "GET /api/jobs should return HTTP 200");

        // Assert data array is present and non-empty
        Assert.assertNotNull(getAllJobsResponse.jsonPath().get("data"),
                "Response 'data' field must not be null");
        List<?> jobs = getAllJobsResponse.jsonPath().getList("data");
        Assert.assertFalse(jobs.isEmpty(),
                "Job listing must contain at least one active job");

        // Assert the created job is present in the listing
        boolean createdJobFound = getAllJobsResponse.jsonPath()
                .getList("data.id").stream()
                .anyMatch(id -> String.valueOf(id).equals(createdJobId));
        Assert.assertTrue(createdJobFound,
                "Created job (ID: " + createdJobId + ") must appear in the job listing");

        // Assert first job has all required fields
        Assert.assertNotNull(getAllJobsResponse.jsonPath().getString("data[0].title"),
                "Job title must not be null");
        Assert.assertNotNull(getAllJobsResponse.jsonPath().getString("data[0].company"),
                "Job company must not be null");
        Assert.assertNotNull(getAllJobsResponse.jsonPath().getString("data[0].location"),
                "Job location must not be null");
        Assert.assertNotNull(getAllJobsResponse.jsonPath().getString("data[0].type"),
                "Job type must not be null");

        log.info("[TC_CR_001] ━━━ ALL STEPS COMPLETED SUCCESSFULLY ━━━ [{} job(s) found]", jobs.size());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  TC_CR_002 — Candidate Applies for a Job
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * TC_CR_002 — Candidate Applies for a Job
     * Verifies that Candidate1 can apply for an active job and that the API
     * responds with status PENDING, the correct cover letter, and the matching job ID.
     *
     * Steps:
     *  Step 01 – Recruiter1 creates a job (to obtain a valid jobId)
     *  Step 02 – Extract jobId from creation response
     *  Step 03 – Candidate1 submits POST /api/applications
     *  Step 04 – Assert: HTTP 200, status=PENDING, coverLetter matches, job.id matches
     */
    @Test(groups = {"Smoke"}, description = "TC_CR_002 - Candidate Applies for a Job")
    public void applyForJob() throws Exception {

        // ── Load per-test static data ─────────────────────────────────────────
        HashMap<String, String> testData = CommonMethod.readTestData(
                "src/main/resources/testdata/applyForJob.json");

        // ── STEP 01 — Recruiter1 creates a job ───────────────────────────────
        log.info("[TC_CR_002] ━━━ STEP 01 ━━━ Recruiter1 creates a job");
        HashMap<String, String> jobData = new HashMap<>();
        jobData.put("title",       testData.get("job.title"));
        jobData.put("description", testData.get("job.description"));
        jobData.put("location",    testData.get("job.location"));
        jobData.put("company",     testData.get("job.company"));
        jobData.put("salary",      testData.get("job.salary"));
        jobData.put("type",        testData.get("job.type"));
        Response createJobResponse = actorHelperForRecruiter1.createJob(jobData);
        Assert.assertEquals(createJobResponse.getStatusCode(), 200,
                "Recruiter1 job creation should return HTTP 200");

        // ── STEP 02 — Extract jobId ───────────────────────────────────────────
        log.info("[TC_CR_002] ━━━ STEP 02 ━━━ Extract jobId from response");
        String jobId = createJobResponse.jsonPath().getString("data.id");
        Assert.assertNotNull(jobId, "Created job ID must not be null");
        log.info("[TC_CR_002] Job ID extracted: {}", jobId);

        // ── STEP 03 — Candidate1 applies for the job ─────────────────────────
        log.info("[TC_CR_002] ━━━ STEP 03 ━━━ Candidate1 submits application for job ID {}", jobId);
        HashMap<String, String> applyData = new HashMap<>();
        applyData.put("jobId",       jobId);
        applyData.put("coverLetter", testData.get("apply.coverLetter"));
        Response applyResponse = actorHelperForCandidate1.applyForJob(applyData);

        // ── STEP 04 — Assertions ──────────────────────────────────────────────
        log.info("[TC_CR_002] ━━━ STEP 04 ━━━ Validating application response");

        // Assert HTTP 200
        Assert.assertEquals(applyResponse.getStatusCode(), 200,
                "POST /api/applications should return HTTP 200");

        // Assert application ID is present
        String applicationId = applyResponse.jsonPath().getString("data.id");
        Assert.assertNotNull(applicationId, "Application ID must not be null after submission");

        // Assert initial status is PENDING
        String status = applyResponse.jsonPath().getString("data.status");
        Assert.assertEquals(status, "PENDING",
                "Newly submitted application status must be 'PENDING'");

        // Assert cover letter matches submitted value
        String responseCoverLetter = applyResponse.jsonPath().getString("data.coverLetter");
        Assert.assertEquals(responseCoverLetter, testData.get("apply.coverLetter"),
                "Cover letter in response must match the submitted cover letter");

        // Assert job ID in response matches the created job
        String responseJobId = applyResponse.jsonPath().getString("data.job.id");
        Assert.assertEquals(responseJobId, jobId,
                "Job ID in application response must match the created job ID");

        // Assert job is still active
        Boolean jobActive = applyResponse.jsonPath().getBoolean("data.job.active");
        Assert.assertTrue(jobActive, "Job must be active at time of application");

        log.info("[TC_CR_002] ━━━ ALL STEPS COMPLETED SUCCESSFULLY ━━━ [Application ID: {}, Status: {}]",
                applicationId, status);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  TC_CR_003 — Candidate Tracks Application Status
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * TC_CR_003 — Candidate Tracks Application Status
     * Verifies that Candidate1 can retrieve their own applications after submitting,
     * and that the tracked application shows the correct status (PENDING),
     * cover letter, and job reference.
     *
     * Steps:
     *  Step 01 – Recruiter1 creates a new job
     *  Step 02 – Candidate1 applies for the job; capture applicationId
     *  Step 03 – Candidate1 calls GET /api/applications/my
     *  Step 04 – Assert: HTTP 200, list not empty, submitted app found by ID
     *  Step 05 – Assert: trackedApp.status=PENDING, coverLetter matches
     */
    @Test(groups = {"Smoke"}, description = "TC_CR_003 - Candidate Tracks Application Status")
    public void trackApplicationStatus() throws Exception {

        // ── Load per-test static data ─────────────────────────────────────────
        HashMap<String, String> testData = CommonMethod.readTestData(
                "src/main/resources/testdata/trackApplicationStatus.json");

        // ── STEP 01 — Recruiter1 creates a job ───────────────────────────────
        log.info("[TC_CR_003] ━━━ STEP 01 ━━━ Recruiter1 creates a job");
        HashMap<String, String> jobData = new HashMap<>();
        jobData.put("title",       testData.get("job.title"));
        jobData.put("description", testData.get("job.description"));
        jobData.put("location",    testData.get("job.location"));
        jobData.put("company",     testData.get("job.company"));
        jobData.put("salary",      testData.get("job.salary"));
        jobData.put("type",        testData.get("job.type"));
        Response createJobResponse = actorHelperForRecruiter1.createJob(jobData);
        Assert.assertEquals(createJobResponse.getStatusCode(), 200,
                "Recruiter1 job creation should return HTTP 200");
        String jobId = createJobResponse.jsonPath().getString("data.id");
        Assert.assertNotNull(jobId, "Created job ID must not be null");
        log.info("[TC_CR_003] Job ID: {}", jobId);

        // ── STEP 02 — Candidate1 applies for the job ─────────────────────────
        log.info("[TC_CR_003] ━━━ STEP 02 ━━━ Candidate1 submits application for job ID {}", jobId);
        HashMap<String, String> applyData = new HashMap<>();
        applyData.put("jobId",       jobId);
        applyData.put("coverLetter", testData.get("apply.coverLetter"));
        Response applyResponse = actorHelperForCandidate1.applyForJob(applyData);
        Assert.assertEquals(applyResponse.getStatusCode(), 200,
                "POST /api/applications should return HTTP 200");
        String submittedApplicationId = applyResponse.jsonPath().getString("data.id");
        Assert.assertNotNull(submittedApplicationId,
                "Submitted application ID must not be null");
        log.info("[TC_CR_003] Application submitted — ID: {}", submittedApplicationId);

        // ── STEP 03 — Candidate1 retrieves their application list ─────────────
        log.info("[TC_CR_003] ━━━ STEP 03 ━━━ Candidate1 calls GET /api/applications/my");
        Response myApplicationsResponse = actorHelperForCandidate1.getMyApplications();

        // ── STEP 04 — Assert HTTP status and list is non-empty ────────────────
        log.info("[TC_CR_003] ━━━ STEP 04 ━━━ Validating application list response");

        Assert.assertEquals(myApplicationsResponse.getStatusCode(), 200,
                "GET /api/applications/my should return HTTP 200");

        List<ApplicationResponse> applications =
                actorHelperForCandidate1.getListOfApplications(myApplicationsResponse);
        Assert.assertFalse(applications.isEmpty(),
                "Candidate1's application list must not be empty after submitting");

        // ── STEP 05 — Find submitted application by ID and assert fields ──────
        log.info("[TC_CR_003] ━━━ STEP 05 ━━━ Locating submitted application ID {} in list",
                submittedApplicationId);
        ApplicationResponse trackedApp = applications.stream()
                .filter(app -> String.valueOf(app.getId()).equals(submittedApplicationId))
                .findFirst()
                .orElse(null);

        Assert.assertNotNull(trackedApp,
                "Submitted application (ID: " + submittedApplicationId + ") must appear in Candidate1's application list");

        // Assert status is PENDING
        Assert.assertEquals(trackedApp.getStatus(), "PENDING",
                "Application status must be 'PENDING' immediately after submission");

        // Assert cover letter matches originally submitted value
        Assert.assertEquals(trackedApp.getCoverLetter(), testData.get("apply.coverLetter"),
                "Cover letter in tracked application must match the originally submitted cover letter");

        log.info("[TC_CR_003] ━━━ ALL STEPS COMPLETED SUCCESSFULLY ━━━ [App ID: {}, Status: {}]",
                submittedApplicationId, trackedApp.getStatus());
    }
}

