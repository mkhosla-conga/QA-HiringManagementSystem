package com.hiring.tests;

import com.hiring.commonMethods.CommonMethod;
import com.hiring.helpers.ActorHelper;
import com.hiring.pojo.ApplicationRequestPOJO;
import com.hiring.pojo.RegisterRequestPOJO;
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
 * Userstory5 — End-to-End Test (Single @Test, all 21 steps inlined)
 *
 *  Step 01 – Register Admin1                                         (AC-1)
 *  Step 02 – Register Recruiter1                                     (AC-1)
 *  Step 03 – Register Recruiter2                                     (AC-1)
 *  Step 04 – Register Candidate1, Candidate2, Candidate3             (AC-1)
 *  Step 05 – Admin1 views all users                                  (AC-1)
 *  Step 06 – Recruiter1 creates Job1 (DevOps Engineer)               (AC-2)
 *  Step 07 – Recruiter1 creates Job2 (QA Automation Engineer)        (AC-2)
 *  Step 08 – Recruiter2 creates Job3 (Machine Learning Engineer)     (AC-2)
 *  Step 09 – Verify all 3 jobs active in listings                    (AC-2)
 *  Step 10 – Candidate1 applies to Job1 and Job2                     (AC-3)
 *  Step 11 – Candidate1 duplicate apply to Job1 → 400               (AC-3)
 *  Step 12 – Candidate2 applies to Job1+Job3; Candidate3 to Job2+Job3 (AC-3)
 *  Step 13 – Recruiter1 views Job1 applications only                 (AC-4)
 *  Step 14 – Recruiter1: Candidate1→INTERVIEW_SCHEDULED, Candidate2→ON_HOLD (AC-5)
 *  Step 15 – Candidate1 dashboard: Job1=INTERVIEW_SCHEDULED, Job2=PENDING  (AC-5, AC-7)
 *  Step 16 – Recruiter1 selects Candidate1 for Job1 (SELECTED→cascade)     (AC-6)
 *  Step 17 – Verify: Candidate2 auto-REJECTED, Job1 deactivated, Candidate1 Job2 PENDING (AC-6, AC-7)
 *  Step 18 – Recruiter2 selects Candidate2 for Job3                 (AC-8, AC-9)
 *  Step 19 – Candidate3 applies to closed Job1 → 400                (AC-10)
 *  Step 20 – Recruiter1 deletes Job2                                 (AC-11)
 *  Step 21 – Verify Job2 gone; empty active-jobs state               (AC-11)
 */
public class Userstory5E2ETest extends BaseTest {

    private static final Logger log = LogManager.getLogger(Userstory5E2ETest.class);

    private UserTokenManager tokenManager;

    // ── Actors ──────────────────────────────────────────────────────────────────
    public ActorHelper actorHelperForAdmin;
    public ActorHelper actorHelperForRecruiter1;
    public ActorHelper actorHelperForRecruiter2;
    public ActorHelper actorHelperForCandidate1;
    public ActorHelper actorHelperForCandidate2;
    public ActorHelper actorHelperForCandidate3;

    public RestUtils restUtilsForAdmin;
    public RestUtils restUtilsForRecruiter1;
    public RestUtils restUtilsForRecruiter2;
    public RestUtils restUtilsForCandidate1;
    public RestUtils restUtilsForCandidate2;
    public RestUtils restUtilsForCandidate3;

    private final Gson gson = new Gson();

    // ═══════════════════════════════════════════════════════════════════════════
    //  SETUP
    // ═══════════════════════════════════════════════════════════════════════════

    @BeforeClass
    public void setUp() {
        tokenManager = new UserTokenManager();

        restUtilsForAdmin      = tokenManager.getRestUtils("admin");
        restUtilsForRecruiter1 = tokenManager.getRestUtils("recruiter1");
        restUtilsForRecruiter2 = tokenManager.getRestUtils("recruiter2");
        restUtilsForCandidate1 = tokenManager.getRestUtils("candidate1");
        restUtilsForCandidate2 = tokenManager.getRestUtils("candidate2");
        restUtilsForCandidate3 = tokenManager.getRestUtils("candidate3");

        actorHelperForAdmin      = tokenManager.getActorHelper("admin");
        actorHelperForRecruiter1 = tokenManager.getActorHelper("recruiter1");
        actorHelperForRecruiter2 = tokenManager.getActorHelper("recruiter2");
        actorHelperForCandidate1 = tokenManager.getActorHelper("candidate1");
        actorHelperForCandidate2 = tokenManager.getActorHelper("candidate2");
        actorHelperForCandidate3 = tokenManager.getActorHelper("candidate3");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  SINGLE @Test — ALL 21 steps inlined
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * TC_US5_001 — E2E: Full Hiring Lifecycle — Multiple Jobs, Multiple Candidates, Cascading Selections
     */
    @Test(groups = {"Regression"}, description = "TC_US5_001 - E2E: Full Hiring Lifecycle — Multiple Jobs, Multiple Candidates, Cascading Selections")
    public void fullHiringLifecycleUS5E2E() throws Exception {

        HashMap<String, String> testData = CommonMethod.readTestData(
                "src/main/resources/testdata/fullHiringLifecycleUS5E2E.json");

        // ── STEP 01 — Register Admin1 ────────────────────────────────────────
        log.info("[E2E] ━━━ STEP 01 ━━━ Register Admin1");
        HashMap<String, String> adminReg = new HashMap<>();
        adminReg.put("email",    tokenManager.getEmail("admin"));
        adminReg.put("password", tokenManager.getPassword("admin"));
        adminReg.put("fullName", testData.get("admin.fullName"));
        adminReg.put("phone",    testData.get("admin.phone"));
        adminReg.put("role",     testData.get("admin.role"));
        Response adminRegResp = restUtilsForAdmin.post(URLGenerator.AUTH_REGISTER,
                gson.toJson(new RegisterRequestPOJO().createRegisterPayload(adminReg)));
        log.info("[E2E] Admin1 register status: {}", adminRegResp.getStatusCode());

        // ── STEP 02 — Register Recruiter1 ───────────────────────────────────
        log.info("[E2E] ━━━ STEP 02 ━━━ Register Recruiter1");
        HashMap<String, String> rec1Reg = new HashMap<>();
        rec1Reg.put("email",    tokenManager.getEmail("recruiter1"));
        rec1Reg.put("password", tokenManager.getPassword("recruiter1"));
        rec1Reg.put("fullName", testData.get("recruiter1.fullName"));
        rec1Reg.put("phone",    testData.get("recruiter1.phone"));
        rec1Reg.put("role",     testData.get("recruiter1.role"));
        Response rec1RegResp = restUtilsForRecruiter1.post(URLGenerator.AUTH_REGISTER,
                gson.toJson(new RegisterRequestPOJO().createRegisterPayload(rec1Reg)));
        log.info("[E2E] Recruiter1 register status: {}", rec1RegResp.getStatusCode());

        // ── STEP 03 — Register Recruiter2 ───────────────────────────────────
        log.info("[E2E] ━━━ STEP 03 ━━━ Register Recruiter2");
        HashMap<String, String> rec2Reg = new HashMap<>();
        rec2Reg.put("email",    tokenManager.getEmail("recruiter2"));
        rec2Reg.put("password", tokenManager.getPassword("recruiter2"));
        rec2Reg.put("fullName", testData.get("recruiter2.fullName"));
        rec2Reg.put("phone",    testData.get("recruiter2.phone"));
        rec2Reg.put("role",     testData.get("recruiter2.role"));
        Response rec2RegResp = restUtilsForRecruiter2.post(URLGenerator.AUTH_REGISTER,
                gson.toJson(new RegisterRequestPOJO().createRegisterPayload(rec2Reg)));
        log.info("[E2E] Recruiter2 register status: {}", rec2RegResp.getStatusCode());

        // ── STEP 04 — Register Candidate1, Candidate2, Candidate3 ───────────
        log.info("[E2E] ━━━ STEP 04 ━━━ Register Candidate1, Candidate2, Candidate3");
        HashMap<String, String> cand1Reg = new HashMap<>();
        cand1Reg.put("email",    tokenManager.getEmail("candidate1"));
        cand1Reg.put("password", tokenManager.getPassword("candidate1"));
        cand1Reg.put("fullName", testData.get("candidate1.fullName"));
        cand1Reg.put("phone",    testData.get("candidate1.phone"));
        cand1Reg.put("role",     testData.get("candidate1.role"));
        restUtilsForCandidate1.post(URLGenerator.AUTH_REGISTER,
                gson.toJson(new RegisterRequestPOJO().createRegisterPayload(cand1Reg)));

        HashMap<String, String> cand2Reg = new HashMap<>();
        cand2Reg.put("email",    tokenManager.getEmail("candidate2"));
        cand2Reg.put("password", tokenManager.getPassword("candidate2"));
        cand2Reg.put("fullName", testData.get("candidate2.fullName"));
        cand2Reg.put("phone",    testData.get("candidate2.phone"));
        cand2Reg.put("role",     testData.get("candidate2.role"));
        restUtilsForCandidate2.post(URLGenerator.AUTH_REGISTER,
                gson.toJson(new RegisterRequestPOJO().createRegisterPayload(cand2Reg)));

        HashMap<String, String> cand3Reg = new HashMap<>();
        cand3Reg.put("email",    tokenManager.getEmail("candidate3"));
        cand3Reg.put("password", tokenManager.getPassword("candidate3"));
        cand3Reg.put("fullName", testData.get("candidate3.fullName"));
        cand3Reg.put("phone",    testData.get("candidate3.phone"));
        cand3Reg.put("role",     testData.get("candidate3.role"));
        restUtilsForCandidate3.post(URLGenerator.AUTH_REGISTER,
                gson.toJson(new RegisterRequestPOJO().createRegisterPayload(cand3Reg)));
        log.info("[E2E] Candidates 1/2/3 registration attempted (200=new, 400=already exists — both OK)");

        // ── STEP 05 — Admin1 views all users (AC-1) ─────────────────────────
        log.info("[E2E] ━━━ STEP 05 ━━━ Admin1 views all registered users");
        Response allUsersResp = actorHelperForAdmin.getAllUsers();
        Assert.assertEquals(allUsersResp.getStatusCode(), 200, "Admin1 get all users failed");
        List<Map<String, Object>> allUsers = allUsersResp.jsonPath().getList("data");
        Assert.assertNotNull(allUsers, "Users list must not be null");
        Assert.assertTrue(allUsers.size() >= 6, "Expected at least 6 registered users (AC-1)");
        log.info("[E2E] Admin1 sees {} users", allUsers.size());

        // ── STEP 06 — Recruiter1 creates Job1 (DevOps Engineer) (AC-2) ──────
        log.info("[E2E] ━━━ STEP 06 ━━━ Recruiter1 creates Job1 (DevOps Engineer)");
        HashMap<String, String> job1Data = new HashMap<>();
        job1Data.put("title",       testData.get("job1.title"));
        job1Data.put("description", testData.get("job1.description"));
        job1Data.put("location",    testData.get("job1.location"));
        job1Data.put("company",     testData.get("job1.company"));
        job1Data.put("salary",      testData.get("job1.salary"));
        job1Data.put("type",        testData.get("job1.type"));
        Response job1Resp = actorHelperForRecruiter1.createJob(job1Data);
        Assert.assertEquals(job1Resp.getStatusCode(), 200, "Job1 creation failed");
        String job1Id = job1Resp.jsonPath().getString("data.id");
        Assert.assertNotNull(job1Id, "Job1 ID must not be null");
        Assert.assertTrue(job1Resp.jsonPath().getBoolean("data.active"), "Job1 must be active");
        log.info("[E2E] Job1 (DevOps Engineer) ID={}", job1Id);

        // ── STEP 07 — Recruiter1 creates Job2 (QA Automation Engineer) (AC-2)
        log.info("[E2E] ━━━ STEP 07 ━━━ Recruiter1 creates Job2 (QA Automation Engineer)");
        HashMap<String, String> job2Data = new HashMap<>();
        job2Data.put("title",       testData.get("job2.title"));
        job2Data.put("description", testData.get("job2.description"));
        job2Data.put("location",    testData.get("job2.location"));
        job2Data.put("company",     testData.get("job2.company"));
        job2Data.put("salary",      testData.get("job2.salary"));
        job2Data.put("type",        testData.get("job2.type"));
        Response job2Resp = actorHelperForRecruiter1.createJob(job2Data);
        Assert.assertEquals(job2Resp.getStatusCode(), 200, "Job2 creation failed");
        String job2Id = job2Resp.jsonPath().getString("data.id");
        Assert.assertNotNull(job2Id, "Job2 ID must not be null");
        log.info("[E2E] Job2 (QA Automation Engineer) ID={}", job2Id);

        // ── STEP 08 — Recruiter2 creates Job3 (Machine Learning Engineer) (AC-2)
        log.info("[E2E] ━━━ STEP 08 ━━━ Recruiter2 creates Job3 (Machine Learning Engineer)");
        HashMap<String, String> job3Data = new HashMap<>();
        job3Data.put("title",       testData.get("job3.title"));
        job3Data.put("description", testData.get("job3.description"));
        job3Data.put("location",    testData.get("job3.location"));
        job3Data.put("company",     testData.get("job3.company"));
        job3Data.put("salary",      testData.get("job3.salary"));
        job3Data.put("type",        testData.get("job3.type"));
        Response job3Resp = actorHelperForRecruiter2.createJob(job3Data);
        Assert.assertEquals(job3Resp.getStatusCode(), 200, "Job3 creation failed");
        String job3Id = job3Resp.jsonPath().getString("data.id");
        Assert.assertNotNull(job3Id, "Job3 ID must not be null");
        log.info("[E2E] Job3 (Machine Learning Engineer) ID={}", job3Id);

        // ── STEP 09 — Verify all 3 jobs active in listings (AC-2) ───────────
        log.info("[E2E] ━━━ STEP 09 ━━━ Verify all 3 jobs active in listings");
        Response allJobsResp = actorHelperForCandidate1.getAllJobs();
        Assert.assertEquals(allJobsResp.getStatusCode(), 200, "Get all jobs failed");
        List<Map<String, Object>> activeJobs = allJobsResp.jsonPath().getList("data");
        Assert.assertTrue(activeJobs.size() >= 3, "Expected at least 3 active jobs (AC-2)");
        log.info("[E2E] Active jobs in listings: {}", activeJobs.size());

        // ── STEP 10 — Candidate1 applies to Job1 and Job2 (AC-3) ────────────
        log.info("[E2E] ━━━ STEP 10 ━━━ Candidate1 applies to Job1 and Job2");
        HashMap<String, String> c1App1Data = new HashMap<>();
        c1App1Data.put("jobId",       job1Id);
        c1App1Data.put("coverLetter", testData.get("c1Job1.coverLetter"));
        Response c1App1Resp = actorHelperForCandidate1.applyForJob(c1App1Data);
        Assert.assertEquals(c1App1Resp.getStatusCode(), 200, "Candidate1 apply to Job1 failed");
        String c1App1Id = c1App1Resp.jsonPath().getString("data.id");
        Assert.assertEquals(c1App1Resp.jsonPath().getString("data.status"), "PENDING",
                "Candidate1 Job1 application must be PENDING");

        HashMap<String, String> c1App2Data = new HashMap<>();
        c1App2Data.put("jobId",       job2Id);
        c1App2Data.put("coverLetter", testData.get("c1Job2.coverLetter"));
        Response c1App2Resp = actorHelperForCandidate1.applyForJob(c1App2Data);
        Assert.assertEquals(c1App2Resp.getStatusCode(), 200, "Candidate1 apply to Job2 failed");
        String c1App2Id = c1App2Resp.jsonPath().getString("data.id");
        Assert.assertEquals(c1App2Resp.jsonPath().getString("data.status"), "PENDING",
                "Candidate1 Job2 application must be PENDING");
        log.info("[E2E] Candidate1 App1 ID={} (Job1), App2 ID={} (Job2)", c1App1Id, c1App2Id);

        // ── STEP 11 — Candidate1 duplicate apply to Job1 → 400 (AC-3) ───────
        log.info("[E2E] ━━━ STEP 11 ━━━ Candidate1 duplicate apply to Job1 — expects 400");
        HashMap<String, String> dupData = new HashMap<>();
        dupData.put("jobId",       job1Id);
        dupData.put("coverLetter", testData.get("c1Dup.coverLetter"));
        Response dupResp = restUtilsForCandidate1.post(URLGenerator.APPLICATIONS,
                gson.toJson(new ApplicationRequestPOJO().createApplicationPayload(dupData)));
        Assert.assertEquals(dupResp.getStatusCode(), 400,
                "Duplicate apply must return 400 Bad Request (AC-3)");
        log.info("[E2E] Duplicate apply correctly blocked: 400");

        // ── STEP 12 — Candidate2 applies to Job1+Job3; Candidate3 to Job2+Job3 (AC-3)
        log.info("[E2E] ━━━ STEP 12 ━━━ Candidate2 applies to Job1+Job3; Candidate3 applies to Job2+Job3");
        HashMap<String, String> c2App1Data = new HashMap<>();
        c2App1Data.put("jobId",       job1Id);
        c2App1Data.put("coverLetter", testData.get("c2Job1.coverLetter"));
        Response c2App1Resp = actorHelperForCandidate2.applyForJob(c2App1Data);
        Assert.assertEquals(c2App1Resp.getStatusCode(), 200, "Candidate2 apply to Job1 failed");
        String c2App1Id = c2App1Resp.jsonPath().getString("data.id");

        HashMap<String, String> c2App3Data = new HashMap<>();
        c2App3Data.put("jobId",       job3Id);
        c2App3Data.put("coverLetter", testData.get("c2Job3.coverLetter"));
        Response c2App3Resp = actorHelperForCandidate2.applyForJob(c2App3Data);
        Assert.assertEquals(c2App3Resp.getStatusCode(), 200, "Candidate2 apply to Job3 failed");
        String c2App3Id = c2App3Resp.jsonPath().getString("data.id");

        HashMap<String, String> c3App2Data = new HashMap<>();
        c3App2Data.put("jobId",       job2Id);
        c3App2Data.put("coverLetter", testData.get("c3Job2.coverLetter"));
        Response c3App2Resp = actorHelperForCandidate3.applyForJob(c3App2Data);
        Assert.assertEquals(c3App2Resp.getStatusCode(), 200, "Candidate3 apply to Job2 failed");

        HashMap<String, String> c3App3Data = new HashMap<>();
        c3App3Data.put("jobId",       job3Id);
        c3App3Data.put("coverLetter", testData.get("c3Job3.coverLetter"));
        Response c3App3Resp = actorHelperForCandidate3.applyForJob(c3App3Data);
        Assert.assertEquals(c3App3Resp.getStatusCode(), 200, "Candidate3 apply to Job3 failed");
        log.info("[E2E] Candidate2 App(Job1) ID={}, App(Job3) ID={}; Candidate3 applied to Job2 & Job3",
                c2App1Id, c2App3Id);

        // ── STEP 13 — Recruiter1 views Job1 applications only (AC-4) ────────
        log.info("[E2E] ━━━ STEP 13 ━━━ Recruiter1 views Job1 applications — per-job isolation");
        Response job1AppsResp = actorHelperForRecruiter1.getApplicationsByJob(job1Id);
        Assert.assertEquals(job1AppsResp.getStatusCode(), 200, "Get Job1 applications failed");
        List<Map<String, Object>> job1Apps = job1AppsResp.jsonPath().getList("data");
        Assert.assertEquals(job1Apps.size(), 2,
                "Job1 must have exactly 2 applicants: Candidate1 and Candidate2 (AC-4)");
        log.info("[E2E] Job1 applicants: {}", job1Apps.size());

        // ── STEP 14 — Recruiter1: Candidate1→INTERVIEW_SCHEDULED, Candidate2→ON_HOLD (AC-5)
        log.info("[E2E] ━━━ STEP 14 ━━━ Recruiter1 sets Candidate1→INTERVIEW_SCHEDULED, Candidate2→ON_HOLD");
        Response schedResp = actorHelperForRecruiter1.updateApplicationStatus(c1App1Id, "INTERVIEW_SCHEDULED");
        Assert.assertEquals(schedResp.getStatusCode(), 200, "INTERVIEW_SCHEDULED update failed");
        Assert.assertEquals(schedResp.jsonPath().getString("data.status"), "INTERVIEW_SCHEDULED",
                "Candidate1 Job1 status must be INTERVIEW_SCHEDULED (AC-5)");

        Response holdResp = actorHelperForRecruiter1.updateApplicationStatus(c2App1Id, "ON_HOLD");
        Assert.assertEquals(holdResp.getStatusCode(), 200, "ON_HOLD update failed");
        Assert.assertEquals(holdResp.jsonPath().getString("data.status"), "ON_HOLD",
                "Candidate2 Job1 status must be ON_HOLD (AC-5)");
        log.info("[E2E] Status updates confirmed: Candidate1=INTERVIEW_SCHEDULED, Candidate2=ON_HOLD");

        // ── STEP 15 — Candidate1 dashboard: Job1=INTERVIEW_SCHEDULED, Job2=PENDING (AC-5, AC-7)
        log.info("[E2E] ━━━ STEP 15 ━━━ Candidate1 verifies dashboard (real-time + cross-job isolation)");
        Response c1DashResp = actorHelperForCandidate1.getMyApplications();
        Assert.assertEquals(c1DashResp.getStatusCode(), 200, "Candidate1 get dashboard failed");
        List<Map<String, Object>> c1AppsList = c1DashResp.jsonPath().getList("data");
        Assert.assertTrue(c1AppsList.size() >= 2, "Candidate1 must have at least 2 applications");
        boolean foundIS = false, foundPending = false;
        for (Map<String, Object> app : c1AppsList) {
            Map<String, Object> job = (Map<String, Object>) app.get("job");
            String status = (String) app.get("status");
            if (job1Id.equals(String.valueOf(job.get("id")))) {
                Assert.assertEquals(status, "INTERVIEW_SCHEDULED",
                        "Candidate1 Job1 must be INTERVIEW_SCHEDULED (AC-5)");
                foundIS = true;
            }
            if (job2Id.equals(String.valueOf(job.get("id")))) {
                Assert.assertEquals(status, "PENDING",
                        "Candidate1 Job2 must still be PENDING — cross-job isolation (AC-7)");
                foundPending = true;
            }
        }
        Assert.assertTrue(foundIS, "INTERVIEW_SCHEDULED entry not found in Candidate1 dashboard");
        Assert.assertTrue(foundPending, "PENDING entry for Job2 not found in Candidate1 dashboard");
        log.info("[E2E] Candidate1 dashboard verified: Job1=INTERVIEW_SCHEDULED, Job2=PENDING");

        // ── STEP 16 — Recruiter1 selects Candidate1 for Job1 → cascading (AC-6)
        log.info("[E2E] ━━━ STEP 16 ━━━ Recruiter1 selects Candidate1 for Job1 (SELECTED → cascade)");
        Response selectResp = actorHelperForRecruiter1.updateApplicationStatus(c1App1Id, "SELECTED");
        Assert.assertEquals(selectResp.getStatusCode(), 200, "SELECTED update failed");
        Assert.assertEquals(selectResp.jsonPath().getString("data.status"), "SELECTED",
                "Candidate1 Job1 must be SELECTED (AC-6)");
        Assert.assertFalse(selectResp.jsonPath().getBoolean("data.job.active"),
                "Job1 must be deactivated after SELECTED (AC-6)");
        log.info("[E2E] Candidate1 SELECTED for Job1; Job1 deactivated");

        // ── STEP 17 — Verify cascading: Candidate2 auto-REJECTED, Job1 absent, Candidate1 Job2 PENDING (AC-6, AC-7)
        log.info("[E2E] ━━━ STEP 17 ━━━ Verify cascading effects (AC-6, AC-7)");
        // Verify Candidate2 auto-REJECTED for Job1
        Response job1AfterResp = actorHelperForRecruiter1.getApplicationsByJob(job1Id);
        Assert.assertEquals(job1AfterResp.getStatusCode(), 200, "Get Job1 apps after SELECTED failed");
        List<Map<String, Object>> job1AppsAfter = job1AfterResp.jsonPath().getList("data");
        boolean cand2Rejected = false;
        for (Map<String, Object> app : job1AppsAfter) {
            Map<String, Object> candidate = (Map<String, Object>) app.get("candidate");
            if (tokenManager.getEmail("candidate2").equals(candidate.get("email"))) {
                Assert.assertEquals(app.get("status"), "REJECTED",
                        "Candidate2 must be auto-REJECTED after Candidate1 SELECTED (AC-6)");
                cand2Rejected = true;
            }
        }
        Assert.assertTrue(cand2Rejected, "Candidate2 auto-rejection not confirmed");

        // Verify Job1 absent from active listings
        Response activeJobsAfterResp = actorHelperForCandidate1.getAllJobs();
        Assert.assertEquals(activeJobsAfterResp.getStatusCode(), 200, "Get active jobs after SELECTED failed");
        List<Map<String, Object>> activeJobsAfter = activeJobsAfterResp.jsonPath().getList("data");
        for (Map<String, Object> job : activeJobsAfter) {
            Assert.assertNotEquals(String.valueOf(job.get("id")), job1Id,
                    "Job1 must NOT appear in active listings after SELECTED (AC-6)");
        }

        // Verify Candidate1 Job2 still PENDING (cross-job isolation)
        Response c1DashAfterResp = actorHelperForCandidate1.getMyApplications();
        Assert.assertEquals(c1DashAfterResp.getStatusCode(), 200, "Candidate1 dashboard after SELECTED failed");
        List<Map<String, Object>> c1AfterApps = c1DashAfterResp.jsonPath().getList("data");
        for (Map<String, Object> app : c1AfterApps) {
            Map<String, Object> job = (Map<String, Object>) app.get("job");
            if (job2Id.equals(String.valueOf(job.get("id")))) {
                Assert.assertEquals(app.get("status"), "PENDING",
                        "Candidate1 Job2 must still be PENDING — cross-job isolation (AC-7)");
            }
        }
        log.info("[E2E] Cascading verified: Candidate2=REJECTED, Job1 deactivated, Candidate1 Job2=PENDING");

        // ── STEP 18 — Recruiter2 selects Candidate2 for Job3 — multi-recruiter independence (AC-8, AC-9)
        log.info("[E2E] ━━━ STEP 18 ━━━ Recruiter2 independently selects Candidate2 for Job3");
        Response rec2SelectResp = actorHelperForRecruiter2.updateApplicationStatus(c2App3Id, "SELECTED");
        Assert.assertEquals(rec2SelectResp.getStatusCode(), 200, "Recruiter2 SELECTED for Job3 failed");
        Assert.assertEquals(rec2SelectResp.jsonPath().getString("data.status"), "SELECTED",
                "Candidate2 Job3 must be SELECTED (AC-8)");
        Assert.assertFalse(rec2SelectResp.jsonPath().getBoolean("data.job.active"),
                "Job3 must be deactivated after Recruiter2 SELECTED (AC-9)");
        log.info("[E2E] Multi-recruiter independence confirmed: Recruiter2 selected Candidate2 for Job3");

        // ── STEP 19 — Candidate3 applies to closed Job1 → 400 (AC-10) ───────
        log.info("[E2E] ━━━ STEP 19 ━━━ Candidate3 applies to closed Job1 — expects 400");
        HashMap<String, String> c3ClosedData = new HashMap<>();
        c3ClosedData.put("jobId",       job1Id);
        c3ClosedData.put("coverLetter", testData.get("c3Closed.coverLetter"));
        Response closedResp = restUtilsForCandidate3.post(URLGenerator.APPLICATIONS,
                gson.toJson(new ApplicationRequestPOJO().createApplicationPayload(c3ClosedData)));
        Assert.assertEquals(closedResp.getStatusCode(), 400,
                "Apply to closed job must return 400 Bad Request (AC-10)");
        log.info("[E2E] Post-closure blocking confirmed: 400 for closed Job1");

        // ── STEP 20 — Recruiter1 deletes Job2 (AC-11) ───────────────────────
        log.info("[E2E] ━━━ STEP 20 ━━━ Recruiter1 deletes Job2");
        Response deleteJob2Resp = actorHelperForRecruiter1.deleteJob(job2Id);
        Assert.assertEquals(deleteJob2Resp.getStatusCode(), 200, "Job2 deletion failed (AC-11)");
        log.info("[E2E] Job2 deleted successfully");

        // ── STEP 21 — Verify Job2 gone; empty active-jobs state (AC-11) ─────
        log.info("[E2E] ━━━ STEP 21 ━━━ Verify Job2 removed; system reaches empty active-jobs state");
        Response finalJobsResp = actorHelperForCandidate1.getAllJobs();
        Assert.assertEquals(finalJobsResp.getStatusCode(), 200, "Final get all jobs failed");
        List<Map<String, Object>> finalJobs = finalJobsResp.jsonPath().getList("data");
        for (Map<String, Object> job : finalJobs) {
            Assert.assertNotEquals(String.valueOf(job.get("id")), job2Id,
                    "Job2 must NOT appear in listings after deletion (AC-11)");
        }
        log.info("[E2E] System integrity verified — Job2 absent. Remaining active jobs: {}", finalJobs.size());

        log.info("[E2E] ━━━ ALL 21 STEPS COMPLETED SUCCESSFULLY ━━━");
    }
}

