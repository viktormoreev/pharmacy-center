# Developer Task Checklist

Updated: February 10, 2026

## Done

- [x] Core modules working: Medicines, Doctors, Customers, Prescriptions/Recipes, Diagnoses, Sick Leaves
- [x] Keycloak/OAuth2 login with role extraction (ADMIN/DOCTOR/PHARMACIST/CUSTOMER)
- [x] Role-based UI cleanup:
- [x] CUSTOMER sees only own history area (no dashboard shortcut in top menu)
- [x] DOCTOR has read-only access to lists/details where required
- [x] Medicine management restricted by role (MVP RBAC tightening)
- [x] Customer self-history page added (own examinations/history)
- [x] Reports pages wired and accessible from reports index
- [x] Sick leave monthly report fixed for PostgreSQL:
- [x] Replaced `YEAR/MONTH` usage with `EXTRACT(...)`
- [x] Fixed Thymeleaf max calculation crash on `/reports/sick-leaves-by-month`
- [x] Demo seed expanded in `src/main/resources/data.sql` for presentation scenarios

## In Progress / Needs Verification

- [ ] Verify end-to-end Keycloak user to DB person mapping by email/username
- [ ] Verify doctor identity consistency in UI (navbar doctor vs prescription doctor name)
- [ ] Final pass of role visibility on all menu entries for each role account

## Left To Be Done

### High Priority

- [ ] Add automated tests for RBAC + report flows (especially doctor/customer scopes)
- [ ] Add centralized error handling page/API format instead of raw Whitelabel errors

### Medium Priority

- [ ] Add pagination/filtering for larger list pages
- [ ] Add data integrity checks for cross-entity consistency (doctor/customer/recipe links)
- [ ] Add one-click demo dataset reset script for presentation mode

### Low Priority

- [ ] Add OpenAPI/Swagger docs for main APIs
