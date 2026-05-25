# Skill: Test Execution

## Purpose
Execute the generated test cases and validate results to ensure all tests pass before proceeding.

## Instructions

1. **Pre-Execution Checks**:
   - Verify `pom.xml` dependencies are resolved: `mvn dependency:resolve`
   - Verify code compiles successfully: `mvn compile -pl . -am`
   - Verify `testng.xml` includes all new test classes.
   - Verify `config.properties` has the correct base URL.

2. **Execute Tests**:
   - Run command: `mvn clean test`
   - Alternatively for specific test class: `mvn test -Dtest=ClassName`
   - For specific test suite: `mvn test -DsuiteXmlFile=testng.xml`

3. **Analyze Results**:
   - Check Maven Surefire reports in `target/surefire-reports/`.
   - Review TestNG output in `test-output/` directory.
   - Identify pass/fail/skip counts.

4. **Handle Failures**:
   - If tests fail due to **code issues**: Go back to `code-generation` and fix.
   - If tests fail due to **environment issues**: Document and flag for manual resolution.
   - If tests fail due to **API changes**: Go back to `requirement-understanding` and re-evaluate.

5. **Success Criteria**:
   - ✅ All tests pass (0 failures, 0 errors).
   - ✅ No compilation errors.
   - ✅ Test execution completes without timeouts.

## Commands Reference

```bash
# Full test execution
mvn clean test

# Run specific test class
mvn test -Dtest=com.hiring.tests.CandidateTest

# Run specific test method
mvn test -Dtest=com.hiring.tests.CandidateTest#testCreateCandidate

# Generate report
mvn surefire-report:report
```

## Output
- Test execution summary (total/pass/fail/skip).
- If all pass: Proceed to `git-push` skill.
- If failures exist: Document failures and loop back for fixes.

## Rules
- Do NOT proceed to `git-push` if any test fails.
- Maximum 3 retry attempts for fixing failures before escalating.
- Always run full suite (not individual tests) for final validation.
- Capture and report execution time.

