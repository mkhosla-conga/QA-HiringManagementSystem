# Skill: POJO Evaluation

## Purpose
Evaluate, create, or update POJO (Plain Old Java Object) classes needed for request/response serialization in the test automation code.

## Instructions

1. **Analyze API Contracts**: Based on the requirements and test cases, identify all request and response body structures.

2. **Check Existing POJOs**: Look in `src/test/java/com/hiring/pojo/` for any existing POJO classes that can be reused or extended.

3. **Create/Update POJOs**:
   - Create a POJO class for each unique request/response body.
   - Follow Java naming conventions (PascalCase for class, camelCase for fields).
   - Include:
     - Private fields with appropriate data types.
     - No-arg constructor (required for Jackson deserialization).
     - Parameterized constructor for convenience.
     - Getters and setters for all fields.

4. **Data Type Mapping**:
   | JSON Type | Java Type |
   |-----------|-----------|
   | string    | String    |
   | number (int) | int / Integer |
   | number (decimal) | double / Double |
   | boolean   | boolean / Boolean |
   | array     | List<T>   |
   | object    | Custom POJO |
   | null      | nullable wrapper types |

5. **Package**: Place all POJOs in `src/test/java/com/hiring/pojo/`

6. **Naming Convention**:
   - Request POJOs: `<Entity>Request.java` (e.g., `CandidateRequest.java`)
   - Response POJOs: `<Entity>Response.java` (e.g., `CandidateResponse.java`)
   - If request and response are the same structure, use `<Entity>.java`

## Output
- List of POJO classes created/updated with their fields.
- Confirmation that POJOs match the API contract.

## Example POJO

```java
package com.hiring.pojo;

public class CandidateRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String position;

    public CandidateRequest() {}

    public CandidateRequest(String firstName, String lastName, String email, String phone, String position) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.position = position;
    }

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    // ... remaining getters/setters
}
```

## Rules
- Always use Jackson-compatible annotations if field names differ from JSON keys (e.g., `@JsonProperty`).
- Do NOT use Lombok — keep POJOs explicit with full getters/setters.
- Ensure POJOs are serializable to/from JSON using Jackson ObjectMapper.
- Reuse existing POJOs when possible; do not create duplicates.

