# Developer Task Checklist

Updated: February 9, 2026

## Current Status Snapshot

- [x] Medicine module (entity, repository, service, API, UI)
- [x] Doctor module (entity, repository, service, UI)
- [x] Customer module (entity, repository, service, API, UI)
- [x] Recipe module (entity relationships, service, API, UI)
- [x] Diagnosis module (entity, repository, service, API, UI)
- [x] Sick Leave module (entity, repository, service, API, UI)
- [x] Dashboard (controller + UI)
- [x] Reports module (service, API, UI templates)
- [x] Keycloak/OAuth2 security integration
- [ ] Doctor API controller (missing)
- [ ] Authentication user lifecycle in app (local register/profile/password screens)
- [ ] Order/transaction system
- [ ] Robust test suite (only minimal Spring Boot test exists)
- [ ] DB migrations (Flyway/Liquibase)

## Evidence-Based Module Status

### 1) Core Medical Modules
- [x] Medicines: implemented
- [x] Doctors: implemented (web CRUD)
- [x] Customers: implemented (web + API CRUD)
- [x] Recipes/Prescriptions: implemented (web + API + relationships)

### 2) Extended Medical Records
- [x] Diagnoses: implemented (web + API + search/filter/statistics)
- [x] Sick Leaves: implemented (web + API + lifecycle actions)

### 3) Reporting and Dashboard
- [x] Dashboard page and controller
- [x] Report service + report controllers
- [x] Report templates for required report categories

### 4) Security
- [x] Spring Security + OAuth2/Keycloak configured
- [x] Route-level role restrictions configured
- [ ] Internal app user registration/profile management not implemented

### 5) Quality and Delivery
- [ ] Unit/integration coverage for services/controllers/repositories
- [ ] Test plan for critical business workflows
- [ ] DB migration tooling and migration scripts under version control

## Priority Tasks (Next)

### High Priority
- [ ] Add automated tests for Customer, Recipe, and Report flows
- [ ] Add migrations with Flyway/Liquibase and remove schema drift risk
- [ ] Add Doctor REST API controller for module parity

### Medium Priority
- [ ] Remove legacy/duplicate Java packages that are no longer used
- [ ] Improve global exception handling and consistent API error format
- [ ] Add pagination for list-heavy endpoints/views

### Low Priority
- [ ] Add OpenAPI/Swagger docs
- [ ] Add UI quality-of-life improvements (toasts, loading states, 404/500 pages)

## Done in This Cleanup

- [x] Revalidated current module status from source code and templates
- [x] Replaced outdated checklist with current state
