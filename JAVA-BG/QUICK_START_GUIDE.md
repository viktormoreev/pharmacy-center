# Quick Start - MVP Presentation State

Updated: February 10, 2026

## What Is Ready

- Keycloak login with roles: `ADMIN`, `DOCTOR`, `PHARMACIST`, `CUSTOMER`
- Main medical flows: customers, doctors, prescriptions/recipes, diagnoses, sick leaves
- Role-based UI restrictions (MVP clean RBAC direction)
- Reports module available from `/reports`
- PostgreSQL fix for sick leave monthly report query
- Expanded demo records in `src/main/resources/data.sql`

## Fast Demo Flow

1. Start infrastructure (`docker-compose up -d` if needed).
2. Run app (`./gradlew bootRun`).
3. Login as each role and verify menu/permissions:
- `admin@pharmacy.com`
- `doctor@pharmacy.com`
- `pharmacist@pharmacy.com`
- `customer@pharmacy.com`
4. Open reports:
- `/reports/common-diagnoses`
- `/reports/doctor-visit-statistics`
- `/reports/sick-leaves-by-month`
5. Show customer self-history and doctor-related flows.

## Known Open Items

- Confirm identity mapping consistency between Keycloak user and DB doctor/customer records
- Final RBAC verification pass on all pages/actions
- Add automated tests for report + RBAC scenarios
- Add migration tool (Flyway/Liquibase)

## Presentation Tip

If data looks stale, reset/reload DB seed from `src/main/resources/data.sql` before demo so all reports show meaningful numbers.
