# Test Implementation Checklist

Updated: February 10, 2026

## Goal

Build a reliable MVP test suite incrementally, starting from fast unit tests and moving to integration/security coverage.

## Phase 1 - Foundation (In Progress)

- [x] Add test roadmap and execution order
- [x] Add unit tests for report mapping logic (`ReportServiceImpl`)
- [x] Ensure test command is stable in local environment (`./gradlew test`)
- [x] Add controller-level unit tests for report pages (`ReportViewController`)

## Phase 2 - Service Unit Tests

- [x] `RecipeServiceImpl` happy path create/update flow
- [x] `SickLeaveServiceImpl` create and status lifecycle checks
- [x] `CustomerServiceImpl` role-sensitive retrieval scenarios
- [x] `DoctorServiceImpl` CRUD and validation edge cases

## Phase 2.5 - API Controller Unit Tests

- [x] `RecipeApiController` delegation tests (CRUD + filter endpoints)

## Phase 3 - Repository Integration Tests (`@DataJpaTest`)

- [x] PostgreSQL-compatible query coverage for report aggregations
- [x] Sick leave by month query regression test
- [x] Diagnosis/patient count query checks

## Phase 4 - Web Layer Tests

- [ ] `ReportViewController` page rendering for each report endpoint
- [x] Security visibility tests per role (doctor/customer/report access rules via `@WebMvcTest`)
- [ ] Customer self-history access control tests
- [x] `DoctorViewController` unit tests (list/create/error handling)
- [x] `CustomerViewController` unit tests (list/search/create/validation handling)

## Phase 5 - Smoke E2E

- [ ] Full workflow: doctor creates examination -> diagnosis/treatment/sick leave visible in reports
- [ ] Customer can only see own history
- [ ] Admin can access all reporting pages

## Notes

- Keep tests deterministic (fixed dates, no flaky random data).
- Prefer unit tests first for speed, then targeted integration tests for SQL/security.
