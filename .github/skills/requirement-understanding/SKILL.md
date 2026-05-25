# Skill: Requirement Understanding

## Purpose
Read and analyze the user story to extract all requirements needed for API test automation.

## Instructions

1. **Read the User Story**: Accept the user story or requirement document provided as input.

2. **Identify API Contracts**:
   - Extract all API endpoints mentioned or implied (HTTP method, URL, headers).
   - Identify request body structure (fields, data types, required/optional).
   - Identify response body structure (fields, status codes, error formats).

3. **Extract Acceptance Criteria**:
   - List all acceptance criteria from the user story.
   - Convert each criterion into a testable condition.

4. **Identify Scenarios**:
   - **Happy Path**: Normal successful flows.
   - **Negative Cases**: Invalid inputs, missing fields, unauthorized access.
   - **Edge Cases**: Boundary values, empty payloads, special characters.
   - **Error Handling**: Expected error responses and status codes.

5. **Document Dependencies**:
   - Pre-requisite data or setup needed.
   - API dependencies (e.g., create before update/delete).
   - Authentication/Authorization requirements.

## Output Format

Produce a structured summary:

```
## Requirements Summary

### API Endpoint(s):
- [METHOD] /endpoint - Description

### Request Structure:
- Field | Type | Required | Description

### Response Structure:
- Field | Type | Description

### Acceptance Criteria:
1. [AC-1] Description
2. [AC-2] Description

### Test Scenarios Identified:
- Positive: [list]
- Negative: [list]
- Edge Cases: [list]

### Dependencies:
- [list]
```

## Rules
- Do NOT make assumptions — only extract what is explicitly stated or clearly implied.
- If information is missing, flag it as "Needs Clarification".
- Ensure every requirement is traceable to the original user story.

