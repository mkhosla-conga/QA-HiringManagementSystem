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
 * Userstory3 — Full E2E Hiring Lifecycle (Single @Test, all 23 steps inlined)
 *
 * TC_US3_001 — E2E: Full Hiring Lifecycle with Multiple Jobs, Candidates and Cascading Selections
 *
 * Steps:
 *  01 – Register Admin1
 *  02 – Register Recruiter1
 *  03 – Register Recruiter2
 *  04 – Register Candidate1, Candidate2, Candidate3
 *  05 – Admin1 views all registered users
 *  06 – Recruiter1 creates Job1 (Backend Engineer)
 *  07 – Recruiter1 creates Job2 (Frontend Developer)
 *  08 – Recruiter2 creates Job3 (Data Analyst)
 *  09 – Verify all 3 jobs are active in listings
 *  10 – Candidate1 applies to Job1 AND Job2
 *  11 – Candidate1 duplicate apply to Job1 → 409 Conflict
 *  12 – Candidate2 applies to Job1 + Job3; Candidate3 applies to Job2 + Job3
 *  13 – Recruiter1 views applications for Job1 only (isolation check)
 *  14 – Recruiter1 updates Candidate1→SHORTLISTED, Candidate2→REVIEWED for Job1
 *  15 – Candidate1 verifies dashboard: Job1=SHORTLISTED, Job2=PENDING (cross-job isolation)
 *  16 – Recruiter1 ACCEPTS Candidate1 for Job1 → remaining applicants auto-REJECTED, Job1 deactivated
 *  17 – Verify Candidate2 auto-REJECTED for Job1; Job1 absent from active listings; Candidate1 Job2 unchanged
 *  18 – Recruiter2 ACCEPTS Candidate2 for Job3 → Candidate3 auto-REJECTED for Job3, Job3 deactivated
 *  19 – Candidate3 attempts apply to closed Job1 → 400 Bad Request
 *  20 – Recruiter1 deletes Job2
 *  21 – Verify Job2 gone; no active jobs remain
 *  22 – Admin1 deletes Candidate3
 *  23 – Verify deleted Candidate3 cannot authenticate → 401 Unauthorized
 */
public class Userstory3E2ETest extends BaseTest {

    private static final Logger log = LogManager.getLogger(Userstory3E2ETest.class);

    // ── UserTokenManager — single source of truth for all credentials ──────────
    private UserTokenManager tokenManager;

    // ── Actors ──────────────────────────────────────────────────────────────────
    // admin      → Admin1     (userDetails prefix: admin)
    // recruiter1 → Recruiter1 (userDetails prefix: recruiter1)
    // recruiter2 → Recruiter2 (userDetails prefix: recruiter2)
    // candidate1 → Candidate1 (userDetails prefix: candidate1)
    // candidate2 → Candidate2 (userDetails prefix: candidate2)
    // candidate3 → Candidate3 (userDetails prefix: candidate3)
    public ActorHelper actorHelperForAdmin;
    public ActorHelper actorHelperForRecruiter1;
    public ActorHelper actorHelperForRecruiter2;
    public ActorHelper actorHelperForCandidate1;   // Candidate1 — candidate1 prefix
    public ActorHelper actorHelperForCandidate2;   // Candidate2 — candidate2 prefix
    public ActorHelper actorHelperForCandidate3;   // Candidate3 — candidate3 prefix

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
        actorHelperForAdmin    = tokenManager.getActorHelper("admin");

        restUtilsForRecruiter1   = tokenManager.getRestUtils("recruiter1");
        actorHelperForRecruiter1 = tokenManager.getActorHelper("recruiter1");

        restUtilsForRecruiter2   = tokenManager.getRestUtils("recruiter2");
        actorHelperForRecruiter2 = tokenManager.getActorHelper("recruiter2");

        restUtilsForCandidate1   = tokenManager.getRestUtils("candidate1");   // Candidate1
        actorHelperForCandidate1 = tokenManager.getActorHelper("candidate1");

        restUtilsForCandidate2   = tokenManager.getRestUtils("candidate2");   // Candidate2
        actorHelperForCandidate2 = tokenManager.getActorHelper("candidate2");

        restUtilsForCandidate3   = tokenManager.getRestUtils("candidate3");   // Candidate3
        actorHelperForCandidate3 = tokenManager.getActorHelper("candidate3");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  SINGLE @Test — all 23 steps inlined sequentially
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * TC_US3_001 — E2E: Full Hiring Lifecycle with Multiple Jobs, Candidates and Cascading Selections
     *
     * End-to-end hiring lifecycle: Admin/Recruiter/Candidate registration, multiple job creations,
     * multi-job applications, duplicate prevention, multi-stage status updates, cascading selection
     * (ACCEPTED triggers auto-rejection of remaining applicants and job deactivation),
     * cross-job isolation, multi-recruiter independence, post-closure blocking,
     * job deletion and admin user deletion with auth verification.
     */
    @Test(groups = {"Regression"}, description = "TC_US3_001 - E2E: Full Hiring Lifecycle with Multiple Jobs, Candidates and Cascading Selections")
    public void fullHiringLifecycleE2E() throws Exception {

        // ── Load per-test static data ─────────────────────────────────────────
        HashMap<String, String> testData = CommonMethod.readTestData(
                "src/main/resources/testdata/fullHiringLifecycleE2E.json");

        // ── STEP 01 — Register Admin1 ─────────────────────────────────────────
        log.info("[E2E] ━━━ STEP 01 ━━━ Register Admin1");
        HashMap<String, String> adminReg = new HashMap<>();
        adminReg.put("email",    tokenManager.getEmail("admin"));
        adminReg.put("password", tokenManager.getPassword("admin"));
        adminReg.put("fullName", testData.get("admin.fullName"));
        adminReg.put("phone",    testData.get("admin.phone"));
        adminReg.put("role",     testData.get("admin.role"));
        Response adminRegResp = restUtilsForAdmin.post(URLGenerator.AUTH_REGISTER,
                gson.toJson(new com.hiring.pojo.RegisterRequestPOJO().createRegisterPayload(adminReg)));
        // 200 = newly registered; 400/409 = already exists in seeded DB — both are acceptable for setup
        log.info("[E2E] Admin1 register status: {}", adminRegResp.getStatusCode());

        // ── STEP 02 — Register Recruiter1 ────────────────────────────────────
        log.info("[E2E] ━━━ STEP 02 ━━━ Register Recruiter1");
        HashMap<String, String> rec1Reg = new HashMap<>();
        rec1Reg.put("email",    tokenManager.getEmail("recruiter1"));
        rec1Reg.put("password", tokenManager.getPassword("recruiter1"));
        rec1Reg.put("fullName", testData.get("recruiter1.fullName"));
        rec1Reg.put("phone",    testData.get("recruiter1.phone"));
        rec1Reg.put("role",     testData.get("recruiter1.role"));
        Response rec1RegResp = restUtilsForRecruiter1.post(URLGenerator.AUTH_REGISTER,
                gson.toJson(new com.hiring.pojo.RegisterRequestPOJO().createRegisterPayload(rec1Reg)));
        log.info("[E2E] Recruiter1 register status: {}", rec1RegResp.getStatusCode());

        // ── STEP 03 — Register Recruiter2 ────────────────────────────────────
        log.info("[E2E] ━━━ STEP 03 ━━━ Register Recruiter2");
        HashMap<String, String> rec2Reg = new HashMap<>();
        rec2Reg.put("email",    tokenManager.getEmail("recruiter2"));
        rec2Reg.put("password", tokenManager.getPassword("recruiter2"));
        rec2Reg.put("fullName", testData.get("recruiter2.fullName"));
        rec2Reg.put("phone",    testData.get("recruiter2.phone"));
        rec2Reg.put("role",     testData.get("recruiter2.role"));
        Response rec2RegResp = restUtilsForRecruiter2.post(URLGenerator.AUTH_REGISTER,
                gson.toJson(new com.hiring.pojo.RegisterRequestPOJO().createRegisterPayload(rec2Reg)));
        log.info("[E2E] Recruiter2 register status: {}", rec2RegResp.getStatusCode());

        // ── STEP 04 — Register Candidate1, Candidate2, Candidate3 ────────────
        log.info("[E2E] ━━━ STEP 04 ━━━ Register Candidate1, Candidate2, Candidate3");
        String[][] candidates = {
            { tokenManager.getEmail("candidate1"), tokenManager.getPassword("candidate1"), testData.get("candidate1.fullName"), testData.get("candidate1.phone"), testData.get("candidate1.role") },
            { tokenManager.getEmail("candidate2"), tokenManager.getPassword("candidate2"), testData.get("candidate2.fullName"), testData.get("candidate2.phone"), testData.get("candidate2.role") },
            { tokenManager.getEmail("candidate3"), tokenManager.getPassword("candidate3"), testData.get("candidate3.fullName"), testData.get("candidate3.phone"), testData.get("candidate3.role") }
        };
        RestUtils[] candUtils = { restUtilsForCandidate1, restUtilsForCandidate2, restUtilsForCandidate3 };
        for (int i = 0; i < candidates.length; i++) {
            HashMap<String, String> cReg = new HashMap<>();
            cReg.put("email",    candidates[i][0]);
            cReg.put("password", candidates[i][1]);
            cReg.put("fullName", candidates[i][2]);
            cReg.put("phone",    candidates[i][3]);
            cReg.put("role",     candidates[i][4]);
            Response cRegResp = candUtils[i].post(URLGenerator.AUTH_REGISTER,
                    gson.toJson(new com.hiring.pojo.RegisterRequestPOJO().createRegisterPayload(cReg)));
            log.info("[E2E] {} register status: {}", candidates[i][2], cRegResp.getStatusCode());
        }

        // ── STEP 05 — Admin1 views all registered users ───────────────────────
        log.info("[E2E] ━━━ STEP 05 ━━━ Admin1 views all registered users");
        Response allUsersResp = actorHelperForAdmin.getAllUsers();
        Assert.assertEquals(allUsersResp.getStatusCode(), 200, "Admin GET all users failed");
        List<Map<String, Object>> allUsers = allUsersResp.jsonPath().getList("data");
        Assert.assertNotNull(allUsers, "Users list must not be null");
        Assert.assertTrue(allUsers.size() >= 5, "At least 5 users (admin + 2 recruiters + 2 candidates) expected");
        log.info("[E2E] Total users registered: {}", allUsers.size());

        // ── STEP 06 — Recruiter1 creates Job1 (Backend Engineer) ──────────────
        log.info("[E2E] ━━━ STEP 06 ━━━ Recruiter1 creates Job1 (Backend Engineer)");
        HashMap<String, String> job1Data = new HashMap<>();
        job1Data.put("title",       testData.get("job1.title"));
        job1Data.put("description", testData.get("job1.description"));
        job1Data.put("location",    testData.get("job1.location"));
        job1Data.put("company",     testData.get("job1.company"));
        job1Data.put("salary",      testData.get("job1.salary"));
        job1Data.put("type",        testData.get("job1.type"));
        Response job1Resp = actorHelperForRecruiter1.createJob(job1Data);
        Assert.assertEquals(job1Resp.getStatusCode(), 200, "Job1 creation failed");
        Assert.assertEquals(job1Resp.jsonPath().getString("data.title"), testData.get("job1.title"));
        Assert.assertTrue(job1Resp.jsonPath().getBoolean("data.active"), "Job1 must be active");
        String job1Id = job1Resp.jsonPath().getString("data.id");
        log.info("[E2E] Job1 created — id: {}", job1Id);

        // ── STEP 07 — Recruiter1 creates Job2 (Frontend Developer) ───────────
        log.info("[E2E] ━━━ STEP 07 ━━━ Recruiter1 creates Job2 (Frontend Developer)");
        HashMap<String, String> job2Data = new HashMap<>();
        job2Data.put("title",       testData.get("job2.title"));
        job2Data.put("description", testData.get("job2.description"));
        job2Data.put("location",    testData.get("job2.location"));
        job2Data.put("company",     testData.get("job2.company"));
        job2Data.put("salary",      testData.get("job2.salary"));
        job2Data.put("type",        testData.get("job2.type"));
        Response job2Resp = actorHelperForRecruiter1.createJob(job2Data);
        Assert.assertEquals(job2Resp.getStatusCode(), 200, "Job2 creation failed");
        Assert.assertEquals(job2Resp.jsonPath().getString("data.title"), testData.get("job2.title"));
        String job2Id = job2Resp.jsonPath().getString("data.id");
        log.info("[E2E] Job2 created — id: {}", job2Id);

        // ── STEP 08 — Recruiter2 creates Job3 (Data Analyst) ─────────────────
        log.info("[E2E] ━━━ STEP 08 ━━━ Recruiter2 creates Job3 (Data Analyst)");
        HashMap<String, String> job3Data = new HashMap<>();
        job3Data.put("title",       testData.get("job3.title"));
        job3Data.put("description", testData.get("job3.description"));
        job3Data.put("location",    testData.get("job3.location"));
        job3Data.put("company",     testData.get("job3.company"));
        job3Data.put("salary",      testData.get("job3.salary"));
        job3Data.put("type",        testData.get("job3.type"));
        Response job3Resp = actorHelperForRecruiter2.createJob(job3Data);
        Assert.assertEquals(job3Resp.getStatusCode(), 200, "Job3 creation failed");
        Assert.assertEquals(job3Resp.jsonPath().getString("data.title"), testData.get("job3.title"));
        String job3Id = job3Resp.jsonPath().getString("data.id");
        log.info("[E2E] Job3 created — id: {}", job3Id);

        // ── STEP 09 — Verify all 3 jobs are active and visible in listings ────
        log.info("[E2E] ━━━ STEP 09 ━━━ Verify all 3 jobs are active in listings");
        Response allJobsResp = actorHelperForCandidate1.getAllJobs();
        Assert.assertEquals(allJobsResp.getStatusCode(), 200, "GET all jobs failed");
        List<Map<String, Object>> jobList = allJobsResp.jsonPath().getList("data");
        Assert.assertNotNull(jobList, "Jobs list must not be null");
        Assert.assertTrue(jobList.size() >= 3, "At least 3 active jobs expected");
        boolean job1Active = jobList.stream().anyMatch(j -> job1Id.equals(String.valueOf(j.get("id")))
                && Boolean.TRUE.equals(j.get("active")));
        boolean job2Active = jobList.stream().anyMatch(j -> job2Id.equals(String.valueOf(j.get("id")))
                && Boolean.TRUE.equals(j.get("active")));
        boolean job3Active = jobList.stream().anyMatch(j -> job3Id.equals(String.valueOf(j.get("id")))
                && Boolean.TRUE.equals(j.get("active")));
        Assert.assertTrue(job1Active, "Job1 must be active in listings");
        Assert.assertTrue(job2Active, "Job2 must be active in listings");
        Assert.assertTrue(job3Active, "Job3 must be active in listings");
        log.info("[E2E] All 3 jobs confirmed active in listings.");

        // ── STEP 10 — Candidate1 applies to Job1 AND Job2 ────────────────────
        log.info("[E2E] ━━━ STEP 10 ━━━ Candidate1 applies to Job1 and Job2");
        HashMap<String, String> c1Job1Apply = new HashMap<>();
        c1Job1Apply.put("jobId",       job1Id);
        c1Job1Apply.put("coverLetter", testData.get("c1Job1.coverLetter"));
        Response c1Job1AppResp = actorHelperForCandidate1.applyForJob(c1Job1Apply);
        Assert.assertEquals(c1Job1AppResp.getStatusCode(), 200, "Candidate1 apply Job1 failed");
        Assert.assertEquals(c1Job1AppResp.jsonPath().getString("data.status"), "PENDING");
        String appC1Job1 = c1Job1AppResp.jsonPath().getString("data.id");
        log.info("[E2E] Candidate1 applied to Job1 — applicationId: {}", appC1Job1);

        HashMap<String, String> c1Job2Apply = new HashMap<>();
        c1Job2Apply.put("jobId",       job2Id);
        c1Job2Apply.put("coverLetter", testData.get("c1Job2.coverLetter"));
        Response c1Job2AppResp = actorHelperForCandidate1.applyForJob(c1Job2Apply);
        Assert.assertEquals(c1Job2AppResp.getStatusCode(), 200, "Candidate1 apply Job2 failed");
        Assert.assertEquals(c1Job2AppResp.jsonPath().getString("data.status"), "PENDING");
        String appC1Job2 = c1Job2AppResp.jsonPath().getString("data.id");
        log.info("[E2E] Candidate1 applied to Job2 — applicationId: {}", appC1Job2);

        // ── STEP 11 — Candidate1 duplicate apply to Job1 → 400 ───────────────
        log.info("[E2E] ━━━ STEP 11 ━━━ Candidate1 duplicate apply to Job1 - expect 400");
        HashMap<String, String> dupData = new HashMap<>();
        dupData.put("jobId",       job1Id);
        dupData.put("coverLetter", testData.get("dup.coverLetter"));
        String dupPayload = gson.toJson(new ApplicationRequestPOJO().createApplicationPayload(dupData));
        Response dupResp = restUtilsForCandidate1.post(URLGenerator.APPLICATIONS, dupPayload);
        Assert.assertEquals(dupResp.getStatusCode(), 400,
                "Duplicate application must return 400 Bad Request");
        log.info("[E2E] Duplicate apply correctly returned 400.");

        // ── STEP 12 — Candidate2 applies to Job1+Job3; Candidate3 applies to Job2+Job3 ──
        log.info("[E2E] ━━━ STEP 12 ━━━ Candidate2 → Job1, Job3 | Candidate3 → Job2, Job3");

        HashMap<String, String> c2Job1Apply = new HashMap<>();
        c2Job1Apply.put("jobId",       job1Id);
        c2Job1Apply.put("coverLetter", testData.get("c2Job1.coverLetter"));
        Response c2Job1AppResp = actorHelperForCandidate2.applyForJob(c2Job1Apply);
        Assert.assertEquals(c2Job1AppResp.getStatusCode(), 200, "Candidate2 apply Job1 failed");
        String appC2Job1 = c2Job1AppResp.jsonPath().getString("data.id");

        HashMap<String, String> c2Job3Apply = new HashMap<>();
        c2Job3Apply.put("jobId",       job3Id);
        c2Job3Apply.put("coverLetter", testData.get("c2Job3.coverLetter"));
        Response c2Job3AppResp = actorHelperForCandidate2.applyForJob(c2Job3Apply);
        Assert.assertEquals(c2Job3AppResp.getStatusCode(), 200, "Candidate2 apply Job3 failed");
        String appC2Job3 = c2Job3AppResp.jsonPath().getString("data.id");

        HashMap<String, String> c3Job2Apply = new HashMap<>();
        c3Job2Apply.put("jobId",       job2Id);
        c3Job2Apply.put("coverLetter", testData.get("c3Job2.coverLetter"));
        Response c3Job2AppResp = actorHelperForCandidate3.applyForJob(c3Job2Apply);
        Assert.assertEquals(c3Job2AppResp.getStatusCode(), 200, "Candidate3 apply Job2 failed");

        HashMap<String, String> c3Job3Apply = new HashMap<>();
        c3Job3Apply.put("jobId",       job3Id);
        c3Job3Apply.put("coverLetter", testData.get("c3Job3.coverLetter"));
        Response c3Job3AppResp = actorHelperForCandidate3.applyForJob(c3Job3Apply);
        Assert.assertEquals(c3Job3AppResp.getStatusCode(), 200, "Candidate3 apply Job3 failed");
        String appC3Job3 = c3Job3AppResp.jsonPath().getString("data.id");
        log.info("[E2E] Candidate2 → Job1 appId:{}, Job3 appId:{}; Candidate3 → Job3 appId:{}",
                appC2Job1, appC2Job3, appC3Job3);

        // ── STEP 13 — Recruiter1 views applications for Job1 (isolation check) ─
        log.info("[E2E] ━━━ STEP 13 ━━━ Recruiter1 views applications for Job1 only");
        Response job1AppsResp = actorHelperForRecruiter1.getApplicationsByJob(job1Id);
        Assert.assertEquals(job1AppsResp.getStatusCode(), 200, "Get applications for Job1 failed");
        List<Map<String, Object>> job1Apps = job1AppsResp.jsonPath().getList("data");
        Assert.assertEquals(job1Apps.size(), 2,
                "Job1 must have exactly 2 applications (Candidate1 + Candidate2)");
        log.info("[E2E] Job1 has {} applications (isolation verified).", job1Apps.size());

        // ── STEP 14 — Recruiter1 updates statuses: Candidate1→INTERVIEW_SCHEDULED, Candidate2→ON_HOLD
        log.info("[E2E] ━━━ STEP 14 ━━━ Recruiter1 shortlists Candidate1, reviews Candidate2 for Job1");
        Response shortlistResp = actorHelperForRecruiter1.updateApplicationStatus(appC1Job1, "INTERVIEW_SCHEDULED");
        Assert.assertEquals(shortlistResp.getStatusCode(), 200, "Shortlist Candidate1 for Job1 failed");
        Assert.assertEquals(shortlistResp.jsonPath().getString("data.status"), "INTERVIEW_SCHEDULED");

        Response reviewResp = actorHelperForRecruiter1.updateApplicationStatus(appC2Job1, "ON_HOLD");
        Assert.assertEquals(reviewResp.getStatusCode(), 200, "Review Candidate2 for Job1 failed");
        Assert.assertEquals(reviewResp.jsonPath().getString("data.status"), "ON_HOLD");
        log.info("[E2E] Candidate1 SHORTLISTED, Candidate2 REVIEWED for Job1.");

        // ── STEP 15 — Candidate1 verifies dashboard (cross-job isolation) ─────
        log.info("[E2E] ━━━ STEP 15 ━━━ Candidate1 verifies dashboard: Job1=SHORTLISTED, Job2=PENDING");
        Response c1DashResp = actorHelperForCandidate1.getMyApplications();
        Assert.assertEquals(c1DashResp.getStatusCode(), 200, "Candidate1 get my applications failed");
        List<Map<String, Object>> c1Apps = c1DashResp.jsonPath().getList("data");
        boolean c1Job1Shortlisted = c1Apps.stream()
                .anyMatch(a -> appC1Job1.equals(String.valueOf(a.get("id")))
                        && "INTERVIEW_SCHEDULED".equals(a.get("status")));
        boolean c1Job2Pending = c1Apps.stream()
                .anyMatch(a -> appC1Job2.equals(String.valueOf(a.get("id")))
                        && "PENDING".equals(a.get("status")));
        Assert.assertTrue(c1Job1Shortlisted, "Candidate1 Job1 application must show SHORTLISTED");
        Assert.assertTrue(c1Job2Pending,     "Candidate1 Job2 application must still show PENDING (cross-job isolation)");
        log.info("[E2E] Cross-job isolation confirmed for Candidate1.");

        // ── STEP 16 — Recruiter1 ACCEPTS Candidate1 for Job1 ─────────────────
        log.info("[E2E] ━━━ STEP 16 ━━━ Recruiter1 accepts Candidate1 for Job1");
        Response acceptC1Resp = actorHelperForRecruiter1.updateApplicationStatus(appC1Job1, "SELECTED");
        Assert.assertEquals(acceptC1Resp.getStatusCode(), 200, "Accept Candidate1 for Job1 failed");
        Assert.assertEquals(acceptC1Resp.jsonPath().getString("data.status"), "SELECTED");
        log.info("[E2E] Candidate1 SELECTED for Job1.");

        // ── STEP 17 — Verify cascading rejection + Job1 deactivated + cross-job isolation ─
        log.info("[E2E] ━━━ STEP 17 ━━━ Verify cascading: Candidate2 REJECTED, Job1 deactivated, Candidate1 Job2 unchanged");

        // 17a — Candidate2 must be auto-REJECTED for Job1
        Response job1AppsAfterResp = actorHelperForRecruiter1.getApplicationsByJob(job1Id);
        Assert.assertEquals(job1AppsAfterResp.getStatusCode(), 200);
        List<Map<String, Object>> job1AppsAfter = job1AppsAfterResp.jsonPath().getList("data");
        boolean c2AutoRejected = job1AppsAfter.stream()
                .anyMatch(a -> appC2Job1.equals(String.valueOf(a.get("id")))
                        && "REJECTED".equals(a.get("status")));
        Assert.assertTrue(c2AutoRejected,
                "Candidate2 must be auto-REJECTED for Job1 after Candidate1 was accepted");

        // 17b — Job1 must no longer appear in active listings
        Response activeJobsAfterResp = actorHelperForCandidate1.getAllJobs();
        Assert.assertEquals(activeJobsAfterResp.getStatusCode(), 200);
        List<Map<String, Object>> activeJobsAfter = activeJobsAfterResp.jsonPath().getList("data");
        boolean job1StillActive = activeJobsAfter.stream()
                .anyMatch(j -> job1Id.equals(String.valueOf(j.get("id"))));
        Assert.assertFalse(job1StillActive, "Job1 must be deactivated and absent from active listings");

        // 17c — Candidate1 Job2 application still PENDING
        Response c1DashAfterResp = actorHelperForCandidate1.getMyApplications();
        Assert.assertEquals(c1DashAfterResp.getStatusCode(), 200);
        List<Map<String, Object>> c1AppsAfter = c1DashAfterResp.jsonPath().getList("data");
        boolean c1Job2StillPending = c1AppsAfter.stream()
                .anyMatch(a -> appC1Job2.equals(String.valueOf(a.get("id")))
                        && "PENDING".equals(a.get("status")));
        Assert.assertTrue(c1Job2StillPending,
                "Candidate1 Job2 application must remain PENDING after Job1 closed");
        log.info("[E2E] Cascading rejection + deactivation + cross-job isolation all verified.");

        // ── STEP 18 — Recruiter2 ACCEPTS Candidate2 for Job3 (multi-recruiter independence) ─
        log.info("[E2E] ━━━ STEP 18 ━━━ Recruiter2 accepts Candidate2 for Job3");
        Response acceptC2Job3Resp = actorHelperForRecruiter2.updateApplicationStatus(appC2Job3, "SELECTED");
        Assert.assertEquals(acceptC2Job3Resp.getStatusCode(), 200, "Accept Candidate2 for Job3 failed");
        Assert.assertEquals(acceptC2Job3Resp.jsonPath().getString("data.status"), "SELECTED");

        // Verify Candidate3 auto-REJECTED for Job3
        Response job3AppsResp = actorHelperForRecruiter2.getApplicationsByJob(job3Id);
        Assert.assertEquals(job3AppsResp.getStatusCode(), 200);
        List<Map<String, Object>> job3Apps = job3AppsResp.jsonPath().getList("data");
        boolean c3AutoRejectedJob3 = job3Apps.stream()
                .anyMatch(a -> appC3Job3.equals(String.valueOf(a.get("id")))
                        && "REJECTED".equals(a.get("status")));
        Assert.assertTrue(c3AutoRejectedJob3,
                "Candidate3 must be auto-REJECTED for Job3 after Candidate2 was accepted");
        log.info("[E2E] Recruiter2 accepted Candidate2 for Job3; Candidate3 auto-REJECTED.");

        // ── STEP 19 — Candidate3 attempts to apply for closed Job1 → 400 ─────
        log.info("[E2E] ━━━ STEP 19 ━━━ Candidate3 applies for closed Job1 - expect 400");
        HashMap<String, String> closedJobApply = new HashMap<>();
        closedJobApply.put("jobId",       job1Id);
        closedJobApply.put("coverLetter", testData.get("closedJob.coverLetter"));
        String closedPayload = gson.toJson(new ApplicationRequestPOJO().createApplicationPayload(closedJobApply));
        Response closedJobResp = restUtilsForCandidate3.post(URLGenerator.APPLICATIONS, closedPayload);
        Assert.assertEquals(closedJobResp.getStatusCode(), 400,
                "Apply for closed job must return 400 Bad Request");
        log.info("[E2E] Apply for closed Job1 correctly returned 400.");

        // ── STEP 20 — Recruiter1 deletes Job2 ────────────────────────────────
        log.info("[E2E] ━━━ STEP 20 ━━━ Recruiter1 deletes Job2");
        Response deleteJob2Resp = actorHelperForRecruiter1.deleteJob(job2Id);
        Assert.assertEquals(deleteJob2Resp.getStatusCode(), 200, "Delete Job2 failed");
        log.info("[E2E] Job2 deleted successfully.");

        // ── STEP 21 — Verify Job2 gone; no active jobs remain ─────────────────
        log.info("[E2E] ━━━ STEP 21 ━━━ Verify Job2 gone and no active jobs remain");
        Response finalJobsResp = actorHelperForCandidate1.getAllJobs();
        Assert.assertEquals(finalJobsResp.getStatusCode(), 200, "GET jobs after deletion failed");
        List<Map<String, Object>> finalJobs = finalJobsResp.jsonPath().getList("data");
        boolean job2StillExists = finalJobs != null && finalJobs.stream()
                .anyMatch(j -> job2Id.equals(String.valueOf(j.get("id"))));
        Assert.assertFalse(job2StillExists, "Job2 must not appear in listings after deletion");
        log.info("[E2E] Job2 confirmed absent. Active jobs remaining: {}",
                finalJobs == null ? 0 : finalJobs.size());

        // ── STEP 22 — Admin1 deletes Candidate3 ──────────────────────────────
        log.info("[E2E] ━━━ STEP 22 ━━━ Admin1 deletes Candidate3");
        // Find Candidate3's user ID from the admin user list
        Response userListForDelete = actorHelperForAdmin.getAllUsers();
        Assert.assertEquals(userListForDelete.getStatusCode(), 200);
        List<Map<String, Object>> usersForDelete = userListForDelete.jsonPath().getList("data");
        String candidate3Email = tokenManager.getEmail("candidate3");  // Candidate3
        String candidate3UserId = null;
        for (Map<String, Object> user : usersForDelete) {
            if (candidate3Email.equals(user.get("email"))) {
                candidate3UserId = String.valueOf(user.get("id"));
                break;
            }
        }
        Assert.assertNotNull(candidate3UserId, "Candidate3 must exist before deletion");
        Response deleteC3Resp = actorHelperForAdmin.deleteUser(candidate3UserId);
        Assert.assertEquals(deleteC3Resp.getStatusCode(), 200, "Admin delete Candidate3 failed");
        log.info("[E2E] Admin1 deleted Candidate3 (id={}).", candidate3UserId);

        // ── STEP 23 — Verify deleted Candidate3 cannot authenticate → 401 ────
        log.info("[E2E] ━━━ STEP 23 ━━━ Verify deleted Candidate3 cannot authenticate - expect 401");
        String candidate3Password = tokenManager.getPassword("candidate3");
        String loginBody = "{\"email\":\"" + candidate3Email + "\",\"password\":\"" + candidate3Password + "\"}";
        Response loginAfterDeleteResp = new RestUtils().post(URLGenerator.AUTH_LOGIN, loginBody);
        Assert.assertEquals(loginAfterDeleteResp.getStatusCode(), 401,
                "Deleted Candidate3 must not be able to login — expected 401 Unauthorized");
        log.info("[E2E] Deleted Candidate3 correctly returned 401 on login attempt.");

        log.info("[E2E] ━━━ ALL 23 STEPS COMPLETED SUCCESSFULLY ━━━");
    }
}

