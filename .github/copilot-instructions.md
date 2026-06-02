# GitHub Copilot — Workspace Instructions
# QA Hiring Management System

## 🔴 MANDATORY — Read Before Every Task

Before responding to ANY task in this workspace, you MUST:

1. **Read** `.github/agents/api-automation-agent.md` — this is the master workflow orchestrator.
2. **Follow its sequential pipeline** (Step 1 → 2 → 3 → 6) for all automation tasks.
3. **Read the relevant skill file(s)** from `.github/skills/` based on what the task requires.

> Do NOT skip this. Do NOT start coding or generating files before reading the agent and skill files.

---

## Agent File (Always Read First)

| File | Purpose |
|------|---------|
| `.github/agents/api-automation-agent.md` | Master orchestrator — defines the full pipeline workflow |

---

## Skill Files (Read Based on Task)

| Skill | File | When to Read |
|-------|------|--------------|
| `testcase-generation` | `.github/skills/testcase-generation/SKILL.md` | Any test case creation, JSON/Excel generation |
| `code-generation` | `.github/skills/code-generation/SKILL.md` | Any Java test class generation |
| `report-generation` | `.github/skills/report-generation/SKILL.md` | Any ExtentReports / HTML report task |
| `requirement-understanding` | `.github/skills/requirement-understanding/SKILL.md` | Any user story analysis |
| `git-publish` | `.github/skills/git-push/SKILL.md` | Any git commit / push / PR task |

---

## Project Summary

- **Language**: Java 11 | **Build**: Maven | **Framework**: TestNG 7.9.0
- **API Library**: RestAssured 5.4.0
- **Reporting**: ExtentReports 5.1.1 → `reports/ExtentReport.html`
- **Package**: `com.hiring.*`
- **Test Data**: `src/main/resources/testdata/`
- **Test Cases**: `src/main/resources/TestCases/data/<UserStoryName>.json`
- **Config**: `src/main/resources/testdata/config.properties`
- **Credentials**: `src/main/resources/testdata/userDetails.properties`

## Key Rules (Always Apply)

- ✅ NEVER hardcode email or password — always use `tokenManager.getEmail/getPassword("prefix")`
- ✅ NEVER use real person names — always use `Admin1`, `Recruiter1`, `Candidate1`, etc.
- ✅ ALWAYS create JSON test case file BEFORE writing any Java test class
- ✅ ALWAYS run `TestCaseGenerator` immediately after creating JSON (before writing test class)
- ✅ ALWAYS follow the single `@Test` method pattern — all steps inlined sequentially
- ✅ ALWAYS update `testng.xml` to include the new test class

