# Skill: Test Case Generation

## Purpose
Write API-driven technical test cases based on the requirements identified in the previous step.

## Instructions

1. **Reference Requirements**: Use the output from the `requirement-understanding` skill as the source of truth.

2. **Generate Test Cases** for each identified scenario:
   - Assign a unique Test Case ID (e.g., `TC_API_001`).
   - Map each test case to a specific requirement/acceptance criteria.
   - Define clear pre-conditions, test steps, and expected results.

3. **Coverage Categories**:
   - **Positive Tests**: Valid inputs producing successful responses (2xx).
   - **Negative Tests**: Invalid inputs producing error responses (4xx).
   - **Boundary Tests**: Min/max values, empty strings, null fields.
   - **Authentication Tests**: Valid/invalid/missing tokens.
   - **CRUD Flow Tests**: Create → Read → Update → Delete lifecycle.

4. **Test Case Format**:

```
### TC_API_XXX: [Test Case Title]
- **Priority**: High / Medium / Low
- **Requirement Ref**: [AC-X]
- **Pre-condition**: [Setup needed]
- **HTTP Method**: GET / POST / PUT / DELETE
- **Endpoint**: /api/endpoint
- **Request Headers**: [headers]
- **Request Body**: [JSON payload]
- **Expected Status Code**: [200/201/400/404/etc.]
- **Expected Response**: [Key assertions on response body]
- **Validation Points**:
  1. [assertion 1]
  2. [assertion 2]
```

5. **Naming Convention**:
   - Test method names: `test<Action><Entity><Condition>`
   - Example: `testCreateCandidateWithValidData`, `testGetCandidateWithInvalidId`

## Output
A comprehensive list of test cases in the format above, organized by endpoint/feature.

## Rules
- Every acceptance criteria must have at least one test case.
- Include at least 1 positive and 1 negative test per endpoint.
- Test cases must be independent — no reliance on execution order (unless explicitly a flow test).
- Use realistic test data values.

