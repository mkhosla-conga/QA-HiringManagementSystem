package com.hiring.utils;

import com.hiring.helpers.ActorHelper;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * UserTokenManager
 *
 * Reads ALL users from {@code src/main/resources/testdata/userDetails.properties},
 * generates a JWT access token for each one via POST /api/auth/login,
 * and stores them in a HashMap keyed by the user prefix.
 *
 * <pre>
 * userDetails.properties keys (prefix → user):
 *   admin      → admin@test.com      (Admin)
 *   recruiter1 → jaydeep@test.com    (Recruiter 1)
 *   recruiter2 → smitha@test.com     (Recruiter 2)
 *   candidate1 → omkar@test.com      (Candidate — Omkar)
 *   candidate2 → prajwal@test.com    (Candidate — Prajwal)
 *   candidate3 → manik@test.com      (Candidate — Manik)
 * </pre>
 *
 * Usage:
 * <pre>
 *   UserTokenManager mgr = new UserTokenManager();
 *
 *   String        token      = mgr.getToken("recruiter1");
 *   RestUtils     restUtils  = mgr.getRestUtils("recruiter1");
 *   ActorHelper   actor      = mgr.getActorHelper("recruiter1");
 * </pre>
 */
public class UserTokenManager {

    private static final String USER_DETAILS_PATH = "src/main/resources/testdata/userDetails.properties";
    private static final String CONFIG_PATH        = "src/main/resources/testdata/config.properties";

    /** token map  →  key: user prefix  (e.g. "admin", "recruiter1", "candidate2") */
    private final HashMap<String, String> tokenMap = new HashMap<>();

    /** email map    →  key: user prefix */
    private final HashMap<String, String> emailMap    = new HashMap<>();

    /** password map →  key: user prefix */
    private final HashMap<String, String> passwordMap = new HashMap<>();

    private String baseUrl;

    // ─────────────────────────────────────────────────────────────────────────
    //  Constructor — loads properties and generates all tokens immediately
    // ─────────────────────────────────────────────────────────────────────────

    public UserTokenManager() {
        Properties config      = loadProperties(CONFIG_PATH);
        Properties userDetails = loadProperties(USER_DETAILS_PATH);

        this.baseUrl = config.getProperty("base.url", "http://localhost:5000");

        // also set on BaseTest.properties so generateAccessToken() still works
        BaseTest.properties = config;

        generateAllTokens(userDetails);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the JWT access token for the given user prefix.
     *
     * @param userKey prefix exactly as in userDetails.properties (e.g. "admin", "recruiter1")
     * @return JWT access token string
     * @throws IllegalArgumentException if the key is not found
     */
    public String getToken(String userKey) {
        String token = tokenMap.get(userKey);
        if (token == null) {
            throw new IllegalArgumentException(
                    "No token found for user key: '" + userKey + "'. "
                    + "Available keys: " + tokenMap.keySet());
        }
        return token;
    }

    /**
     * Returns a {@link RestUtils} pre-configured with the JWT token for the given user.
     *
     * @param userKey prefix (e.g. "recruiter1")
     * @return RestUtils instance with bearer token set
     */
    public RestUtils getRestUtils(String userKey) {
        return new RestUtils(getToken(userKey));
    }

    /**
     * Returns an {@link ActorHelper} pre-configured with the JWT token for the given user.
     *
     * @param userKey prefix (e.g. "candidate2")
     * @return ActorHelper instance ready to call API methods
     */
    public ActorHelper getActorHelper(String userKey) {
        return new ActorHelper(getRestUtils(userKey));
    }

    /**
     * Returns the email address registered for the given user key.
     *
     * @param userKey prefix (e.g. "candidate1")
     * @return email string
     */
    public String getEmail(String userKey) {
        return emailMap.get(userKey);
    }

    /**
     * Returns the password for the given user key (read from userDetails.properties).
     *
     * @param userKey prefix (e.g. "candidate1")
     * @return password string
     */
    public String getPassword(String userKey) {
        String password = passwordMap.get(userKey);
        if (password == null) {
            throw new IllegalArgumentException(
                    "No password found for user key: '" + userKey + "'. "
                    + "Available keys: " + passwordMap.keySet());
        }
        return password;
    }

    /**
     * Returns an unmodifiable view of all loaded token entries.
     * Key = user prefix, Value = JWT token.
     */
    public Map<String, String> getAllTokens() {
        return java.util.Collections.unmodifiableMap(tokenMap);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Internal helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Iterates over userDetails.properties, collects all unique user prefixes
     * (keys ending in ".email"), then generates a login token for each.
     *
     * Prefix discovery is dynamic — adding a new user to the properties file
     * is enough for it to be picked up automatically on the next run.
     */
    private void generateAllTokens(Properties userDetails) {
        // ── 1. Collect unique prefixes ────────────────────────────────────────
        Set<String> prefixes = new LinkedHashSet<>();
        for (String key : userDetails.stringPropertyNames()) {
            if (key.endsWith(".email")) {
                prefixes.add(key.substring(0, key.lastIndexOf(".email")));
            }
        }

        System.out.println("[UserTokenManager] Found " + prefixes.size() + " user(s) in userDetails.properties: " + prefixes);

        // ── 2. Generate a token for each prefix ───────────────────────────────
        for (String prefix : prefixes) {
            String email    = userDetails.getProperty(prefix + ".email");
            String password = userDetails.getProperty(prefix + ".password");

            if (email == null || password == null) {
                System.err.println("[UserTokenManager] WARN: Missing email or password for prefix '" + prefix + "' — skipped.");
                continue;
            }

            try {
                String token = login(email, password);
                tokenMap.put(prefix, token);
                emailMap.put(prefix, email);
                passwordMap.put(prefix, password);
                System.out.println("[UserTokenManager] ✅ Token generated for [" + prefix + "] → " + email);
            } catch (Exception e) {
                System.err.println("[UserTokenManager] ❌ Token generation FAILED for [" + prefix + "] → " + email + " | " + e.getMessage());
                // Do not rethrow — other users can still be loaded
            }
        }

        System.out.println("[UserTokenManager] Token map ready. Keys: " + tokenMap.keySet());
    }

    /**
     * Calls POST /api/auth/login and returns the access token.
     *
     * @param email    user email
     * @param password user password
     * @return JWT access token string
     */
    private String login(String email, String password) {
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
                .when()
                .post(baseUrl + "/api/auth/login");

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Login failed for [" + email + "] — HTTP "
                    + response.getStatusCode() + " | " + response.getBody().asString());
        }

        String token = response.jsonPath().getString("data.token");
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Login succeeded but token was null/empty for [" + email + "]");
        }
        return token;
    }

    /**
     * Loads a {@link Properties} file from the given path.
     *
     * @param path relative or absolute file path
     * @return loaded Properties object
     */
    private Properties loadProperties(String path) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties file: " + path + " | " + e.getMessage());
        }
        return props;
    }
}

