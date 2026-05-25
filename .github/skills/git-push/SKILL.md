# Skill: Git Push

## Purpose
If all test cases pass successfully, commit the changes and create a Pull Request for review.

## Instructions

1. **Pre-Push Validation**:
   - Confirm all tests passed in the previous step (0 failures).
   - Ensure no uncommitted debug code or hardcoded credentials.
   - Verify `.gitignore` excludes build artifacts (`target/`, `test-output/`, `logs/`).

2. **Branch Strategy**:
   - Create a feature branch from `main`: `feature/<story-id>-<short-description>`
   - Example: `feature/US-101-candidate-api-tests`

3. **Git Commands**:

```bash
# Create and switch to feature branch
git checkout -b feature/<story-id>-<short-description>

# Stage all changes
git add .

# Commit with meaningful message
git commit -m "feat(tests): add API test automation for <feature>

- Added test cases for <endpoint(s)>
- Created POJOs for request/response
- Updated testng.xml with new test classes
- All tests passing

Ref: <story-id>"

# Push to remote
git push origin feature/<story-id>-<short-description>
```

4. **Create Pull Request**:
   - **Title**: `[QA] API Test Automation - <Feature Name>`
   - **Description** must include:
     - Summary of what was automated.
     - List of test cases added.
     - Test execution results (pass count).
     - Link to the user story/requirement.
   - **Labels**: `qa`, `automation`, `api-tests`
   - **Reviewers**: Assign appropriate team reviewers.

5. **PR Description Template**:

```markdown
## Summary
Automated API test cases for [Feature Name].

## Test Cases Added
| Test Case ID | Description | Status |
|---|---|---|
| TC_API_001 | ... | ✅ Pass |
| TC_API_002 | ... | ✅ Pass |

## Execution Results
- **Total**: X
- **Passed**: X
- **Failed**: 0
- **Skipped**: 0

## Requirements Reference
- User Story: [ID]
- Acceptance Criteria covered: [list]

## Files Changed
- `src/test/java/com/hiring/tests/...`
- `src/test/java/com/hiring/pojo/...`
- `src/test/java/com/hiring/endpoints/...`
- `testng.xml`
```

## Output
- Feature branch created and pushed.
- Pull Request created with full description.
- PR link shared as final output.

## Rules
- NEVER push directly to `main` branch.
- NEVER commit test data with sensitive/real credentials.
- Ensure commit messages follow conventional commit format.
- Do NOT create a PR if any test is failing.
- Squash commits if there are multiple fix iterations.

