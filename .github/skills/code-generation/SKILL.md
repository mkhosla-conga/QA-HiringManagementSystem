# Skill: Code Generation

## Purpose
Generate the actual automated test code using RestAssured and TestNG, based on the test cases and POJOs from previous steps.

## Instructions

1. **Reference Inputs**:
   - Test cases from `testcase-generation` skill.
   - POJOs from `pojo-evaluation` skill.
   - Existing project patterns from `src/test/java/com/hiring/`.

2. **Follow Project Structure**:
   - Test classes go in: `src/test/java/com/hiring/tests/`
   - All test classes must extend `BaseTest`.
   - Use `EndPoints` class for URL paths.
   - Use `RestUtils` for common HTTP operations.
   - Use POJOs for request/response bodies.

3. **Test Class Structure**:

```java
package com.hiring.tests;

import com.hiring.base.BaseTest;
import com.hiring.endpoints.EndPoints;
import com.hiring.pojo.EntityRequest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EntityTest extends BaseTest {

    @Test(description = "Description matching test case")
    public void testMethodName() {
        // Arrange
        EntityRequest payload = new EntityRequest(...);

        // Act
        Response response = request
            .body(payload)
            .when()
            .post(EndPoints.ENDPOINT);

        // Assert
        Assert.assertEquals(response.getStatusCode(), 201);
        Assert.assertNotNull(response.jsonPath().getString("id"));
    }
}
```

4. **Coding Standards**:
   - Use `@Test` annotation with `description` attribute.
   - Use `priority` attribute for ordered execution when needed.
   - Use `@DataProvider` for parameterized tests.
   - Add meaningful assertion messages.
   - Follow AAA pattern: Arrange → Act → Assert.
   - Use `response.jsonPath()` for response field extraction.
   - Log important information using Log4j.

5. **Assertion Best Practices**:
   - Assert status code first.
   - Assert response body fields.
   - Assert response headers when relevant.
   - Use `Assert.assertEquals`, `Assert.assertNotNull`, `Assert.assertTrue`.

6. **Update testng.xml**: Add new test classes to `testng.xml` for execution.

7. **Update EndPoints**: Add any new endpoint constants to `EndPoints.java`.

## Output
- Fully implemented test classes.
- Updated `testng.xml` with new test classes.
- Updated `EndPoints.java` if new endpoints are needed.

## Rules
- Every generated test method must map to a test case from Step 2.
- Do NOT hardcode base URLs — use `BaseTest` setup.
- Do NOT hardcode test data inline — use POJOs or test data files.
- Handle test dependencies using `dependsOnMethods` when needed.
- Ensure tests are runnable independently where possible.

