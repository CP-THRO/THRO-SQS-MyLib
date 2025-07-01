# Test Strategy

## 1. Implemented Test

### Unit Tests

- **Backend:**
  - Framework: JUnit 5
  - Coverage reports generated with JaCoCo
  - Purpose:
    - Test individual methods in services and controllers in isolation.
    - Validate business logic without external dependencies.

- **Frontend:**
  - Framework: Vitest
  - Coverage reports generated in lcov format
  - Purpose:
    - Test individual Vue.js components and composables in isolation.
    - Ensure UI logic behaves as expected.

Goal: Achieve precise validation of small code units to detect defects early.


### Integration Tests

- **Backend:**
  - Framework: JUnit 5 (SpringBootTest)
  - Approach:
    - Start the full backend context.
    - Test the collaboration between controllers, services, repositories, and security components.
  - Purpose:
    - Ensure backend layers work correctly together.
    - Validate database interactions and security flows.

Goal: Detect integration issues that unit tests cannot cover.


### End-to-End (E2E) Tests

- **Framework:** Playwright
- Scope:
  - Automates tests covering user journeys through the entire application stack.
  - Tests frontend and backend integration from a user perspective.
- Main scenarios covered:
  - User login and authentication flows
  - Searching for books
  - Adding books to library or wishlist
  - Viewing book details

Goal: Validate that critical user flows work as intended across all system layers.


### Security Tests

- **Integration Test Security Checks:**
  - Simulate requests to secured endpoints with invalid or missing JWT tokens.
  - Ensure unauthorized requests are rejected with appropriate HTTP status codes.

- **OWASP ZAP Scans:**
  - Automated scans to detect common web vulnerabilities, including:
    - SQL Injection
    - Cross-Site Scripting (XSS)
    - Other OWASP Top 10 risks

Goal: Identify and mitigate security vulnerabilities before release.


### Static Code Analysis

- Tools:
  - SonarQube (analyzes both backend and frontend code)
  - JaCoCo for backend coverage
  - lcov for frontend coverage
- Checks performed:
  - Code coverage (target: ≥80%)
  - Code duplication
  - Detection of anti-patterns and code smells
  - Security hotspots

Goal: Maintain high code quality and reduce technical debt.


## 2. Automation

- All tests and static analysis run automatically in the GitHub Actions pipeline on every push.
- Test reports and coverage artifacts are uploaded for review after each build.
- Pipeline ensures:
  - Fast feedback on code changes

Goal: Integrate quality assurance directly into the development workflow to detect issues early and maintain continuous delivery standards.