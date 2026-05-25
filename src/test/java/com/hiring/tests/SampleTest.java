package com.hiring.tests;

import com.hiring.base.BaseTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Sample test class to validate the test setup.
 */
public class SampleTest extends BaseTest {

    @Test(description = "Verify API health check endpoint")
    public void testHealthCheck() {
        Response response = request
                .when()
                .get("/health");

        Assert.assertEquals(response.getStatusCode(), 200, "Health check should return 200");
    }
}

