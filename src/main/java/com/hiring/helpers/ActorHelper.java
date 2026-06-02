package com.hiring.helpers;

import com.google.gson.Gson;
import com.hiring.pojo.*;
import com.hiring.utils.RestUtils;
import com.hiring.utils.URLGenerator;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * ActorHelper - The main orchestration class for the project.
 * 
 * Responsibilities:
 * 1. Creates payloads using specific POJOs.
 * 2. Passes the payload to RestUtils to perform CRUD operations.
 * 3. Returns the Response back to the test case.
 *
 * Test classes should use ActorHelper methods instead of calling RestUtils directly.
 */
public class ActorHelper {

    public RestUtils restUtils;
    Gson gson = new Gson();

    public ActorHelper(RestUtils restUtils) {
        this.restUtils = restUtils;
    }

    // ======================== AUTHENTICATION APIs ========================

    /**
     * Register a new user.
     * Calls POJO's createRegisterPayload() → converts POJO to JSON via Gson → sends to RestUtils.
     */
    public Response registerUser(HashMap<String, String> testData) throws Exception {
        String payload = gson.toJson(new RegisterRequestPOJO().createRegisterPayload(testData));
        System.out.println("Register Payload: " + payload);
        Response response = restUtils.post(URLGenerator.AUTH_REGISTER, payload);
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }

    /**
     * Login and get JWT token.
     * Calls POJO's createLoginPayload() → converts POJO to JSON via Gson → sends to RestUtils.
     */
    public Response loginUser(HashMap<String, String> testData) throws Exception {
        String payload = gson.toJson(new LoginRequestPOJO().createLoginPayload(testData));
        System.out.println("Login Payload: " + payload);
        Response response = restUtils.post(URLGenerator.AUTH_LOGIN, payload);
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }

    // ======================== JOBS APIs ========================

    /**
     * Create a new job (Recruiter only).
     * Calls POJO's createJobPayload() → converts POJO to JSON via Gson → sends to RestUtils.
     */
    public Response createJob(HashMap<String, String> testData) throws Exception {
        String payload = gson.toJson(new JobRequestPOJO().createJobPayload(testData));
        System.out.println("Job Payload: " + payload);
        Response response = restUtils.post(URLGenerator.JOBS, payload);
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }

    /**
     * Update a job by ID.
     * Calls POJO's createJobPayload() → converts POJO to JSON via Gson → sends to RestUtils.
     */
    public Response updateJob(String jobId, HashMap<String, String> testData) throws Exception {
        String payload = gson.toJson(new JobRequestPOJO().createJobPayload(testData));
        System.out.println("Update Job Payload: " + payload);
        String url = RestUtils.replacePathParam(URLGenerator.JOB_BY_ID, "id", jobId);
        Response response = restUtils.put(url, payload);
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }

    /**
     * Get all active jobs.
     */
    public Response getAllJobs() throws Exception {
        Response response = restUtils.get(URLGenerator.JOBS);
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }

    /**
     * Get job by ID.
     */
    public Response getJobById(String jobId) throws Exception {
        String url = RestUtils.replacePathParam(URLGenerator.JOB_BY_ID, "id", jobId);
        Response response = restUtils.get(url);
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }

    /**
     * Delete a job by ID.
     */
    public Response deleteJob(String jobId) throws Exception {
        String url = RestUtils.replacePathParam(URLGenerator.JOB_BY_ID, "id", jobId);
        Response response = restUtils.delete(url);
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }

    // ======================== APPLICATIONS APIs ========================

    /**
     * Apply for a job (Candidate only).
     * Calls POJO's createApplicationPayload() → converts POJO to JSON via Gson → sends to RestUtils.
     */
    public Response applyForJob(HashMap<String, String> testData) throws Exception {
        String payload = gson.toJson(new ApplicationRequestPOJO().createApplicationPayload(testData));
        System.out.println("Application Payload: " + payload);
        Response response = restUtils.post(URLGenerator.APPLICATIONS, payload);
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }

    /**
     * Get my applications (Candidate only).
     */
    public Response getMyApplications() throws Exception {
        Response response = restUtils.get(URLGenerator.MY_APPLICATIONS);
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }

    /**
     * Get applicants for a job (Recruiter only).
     */
    public Response getApplicationsByJob(String jobId) throws Exception {
        String url = RestUtils.replacePathParam(URLGenerator.APPLICATIONS_BY_JOB, "jobId", jobId);
        Response response = restUtils.get(url);
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }

    /**
     * Update application status (e.g., SELECTED, INTERVIEW_SCHEDULED, REJECTED, ON_HOLD).
     */
    public Response updateApplicationStatus(String applicationId, String status) throws Exception {
        String url = RestUtils.replacePathParam(URLGenerator.APPLICATION_STATUS, "id", applicationId);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("status", status);
        Response response = restUtils.putWithQueryParams(url, queryParams);
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }

    // ======================== USER PROFILE APIs ========================

    /**
     * Get current user profile.
     */
    public Response getUserProfile() throws Exception {
        Response response = restUtils.get(URLGenerator.USER_PROFILE);
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }

    /**
     * Update user profile.
     * Calls POJO's createUserProfilePayload() → converts POJO to JSON via Gson → sends to RestUtils.
     */
    public Response updateUserProfile(HashMap<String, String> testData) throws Exception {
        String payload = gson.toJson(new UserProfileRequestPOJO().createUserProfilePayload(testData));
        System.out.println("Profile Payload: " + payload);
        Response response = restUtils.put(URLGenerator.USER_PROFILE, payload);
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }

    // ======================== FILE UPLOAD API ========================

    /**
     * Upload resume (multipart/form-data).
     */
    public Response uploadResume(String filePath) throws Exception {
        Response response = restUtils.uploadFile(URLGenerator.UPLOAD_RESUME, filePath, "resume");
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }

    // ======================== ADMIN APIs ========================

    /**
     * List all users (Admin only).
     */
    public Response getAllUsers() throws Exception {
        Response response = restUtils.get(URLGenerator.ADMIN_USERS);
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }

    /**
     * Delete a user by ID (Admin only).
     */
    public Response deleteUser(String userId) throws Exception {
        String url = RestUtils.replacePathParam(URLGenerator.ADMIN_USER_BY_ID, "id", userId);
        Response response = restUtils.delete(url);
        response.prettyPrint();
        if (response.getStatusCode() != 200) {
            throw new Exception("Failure");
        }
        return response;
    }
}
