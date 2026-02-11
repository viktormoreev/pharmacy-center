# API Reference

REST API reference for Digital Pharmacy MVP.

## 1. Base URL and Auth

- Base URL: `http://localhost:8084`
- API prefix: `/api`
- Auth:
  - Browser UI login via Keycloak/OIDC session.
  - API calls can also use Bearer JWT from the same Keycloak realm.
- CSRF:
  - Disabled for `/api/**` in `SecurityConfig`, so API write operations do not require CSRF token.

## 2. Roles and Access

Role names in code: `ADMIN`, `PHARMACIST`, `DOCTOR`, `CUSTOMER`.

- `DOCTOR | PHARMACIST | ADMIN`:
  - `/api/recipes/**`
  - `/api/diagnoses/**`
  - `/api/sick-leaves/**`
  - `/api/reports/**`
- Medicines:
  - `GET /api/medicines/**` -> `DOCTOR | PHARMACIST | ADMIN`
  - `POST/PUT/DELETE /api/medicines/**` -> `PHARMACIST | ADMIN`
- Customers API:
  - `/api/customers/**` -> `PHARMACIST | ADMIN`
- `CUSTOMER`:
  - No dedicated `/api/my/**` REST endpoints currently; customer self-history is available via UI routes (`/my/**`).

Note: `SecurityConfig` contains matcher for `/api/doctors/**`, but there is currently no `DoctorApiController` implemented.

## 3. Common Conventions

- Content type: `application/json`
- Date format: `yyyy-MM-dd`
- Typical success codes:
  - `200 OK` for reads and most updates
  - `201 Created` for customer create
  - `204 No Content` for delete operations returning empty body
- Error behavior:
  - Validation/business errors return HTTP `4xx` (e.g., `400`, `404`)
  - Unhandled exceptions return HTTP `500` (Spring default error page/JSON)

## 4. Core DTOs/Enums

### 4.1 Medicine DTO

```json
{
  "id": 1,
  "name": "Paracetamol",
  "ageAppropriateness": 6,
  "needsRecipe": false
}
```

### 4.2 Customer DTO

```json
{
  "id": 1,
  "name": "Bob Customer",
  "age": 34,
  "email": "customer@pharmacy.com",
  "phone": "+359888123456",
  "address": "Sofia",
  "dateOfBirth": "1992-03-11",
  "allergies": "Penicillin",
  "medicalHistory": "Asthma",
  "insuranceNumber": "INS-10001",
  "active": true
}
```

### 4.3 Enums

- `RecipeStatus`: `ACTIVE | FULFILLED | EXPIRED | CANCELLED`
- `SickLeaveStatus`: `ACTIVE | COMPLETED | CANCELLED | EXTENDED`
- `DiagnosisSeverity`: `MILD | MODERATE | SEVERE | CRITICAL`

## 5. Endpoints

## 5.1 Medicines (`/api/medicines`)

| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/api/medicines` | DOCTOR, PHARMACIST, ADMIN | List medicines |
| GET | `/api/medicines/{id}` | DOCTOR, PHARMACIST, ADMIN | Get medicine by ID |
| POST | `/api/medicines` | PHARMACIST, ADMIN | Create medicine |
| PUT | `/api/medicines/{id}` | PHARMACIST, ADMIN | Update medicine |
| DELETE | `/api/medicines/{id}` | PHARMACIST, ADMIN | Delete medicine |
| GET | `/api/medicines/by-name/{name}` | DOCTOR, PHARMACIST, ADMIN | Exact name filter |
| GET | `/api/medicines/by-name-starts-with/{name}` | DOCTOR, PHARMACIST, ADMIN | Prefix filter |
| GET | `/api/medicines/by-name/{name}/age-appropriateness/{age}` | DOCTOR, PHARMACIST, ADMIN | Name + age filter |
| GET | `/api/medicines/age-appropriateness/{age}/needs-recipe/{needsRecipe}` | DOCTOR, PHARMACIST, ADMIN | Age + prescription requirement filter |

Create request example:

```json
{
  "name": "Ibuprofen",
  "ageAppropriateness": 12,
  "needsRecipe": false
}
```

## 5.2 Recipes (`/api/recipes`)

| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/api/recipes` | DOCTOR, PHARMACIST, ADMIN | List all examinations/prescriptions |
| GET | `/api/recipes/{id}` | DOCTOR, PHARMACIST, ADMIN | Get recipe by ID |
| POST | `/api/recipes` | DOCTOR, PHARMACIST, ADMIN | Create recipe |
| PUT | `/api/recipes/{id}` | DOCTOR, PHARMACIST, ADMIN | Update recipe |
| DELETE | `/api/recipes/{id}` | DOCTOR, PHARMACIST, ADMIN | Delete recipe |
| GET | `/api/recipes/creation-date/{creationDate}/id/{id}` | DOCTOR, PHARMACIST, ADMIN | Filter by date and doctor ID |
| GET | `/api/recipes/creation-date/{creationDate}/doctor-name-contains/{doctorName}` | DOCTOR, PHARMACIST, ADMIN | Filter by date and doctor name |

Minimal create request example:

```json
{
  "creationDate": "2026-02-10",
  "doctor": { "id": 1 },
  "customer": { "id": 2 },
  "status": "ACTIVE",
  "diagnosis": "Acute bronchitis",
  "notes": "Rest and hydration",
  "sickLeave": true,
  "sickLeaveDays": 5,
  "sickLeaveStartDate": "2026-02-11"
}
```

## 5.3 Customers (`/api/customers`)

| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/api/customers` | PHARMACIST, ADMIN | List customers |
| GET | `/api/customers/active` | PHARMACIST, ADMIN | List active customers |
| GET | `/api/customers/{id}` | PHARMACIST, ADMIN | Get customer by ID |
| GET | `/api/customers/search?name={name}` | PHARMACIST, ADMIN | Search by name |
| GET | `/api/customers/age-range?minAge={min}&maxAge={max}` | PHARMACIST, ADMIN | Filter by age range |
| GET | `/api/customers/allergy?allergy={text}` | PHARMACIST, ADMIN | Filter by allergy |
| POST | `/api/customers` | PHARMACIST, ADMIN | Create customer |
| PUT | `/api/customers/{id}` | PHARMACIST, ADMIN | Update customer |
| DELETE | `/api/customers/{id}` | PHARMACIST, ADMIN | Soft delete (`active=false`) |
| DELETE | `/api/customers/{id}/hard` | PHARMACIST, ADMIN | Hard delete |
| GET | `/api/customers/email-exists?email={email}` | PHARMACIST, ADMIN | Check email uniqueness |

## 5.4 Diagnoses (`/api/diagnoses`)

| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/api/diagnoses` | DOCTOR, PHARMACIST, ADMIN | List diagnoses |
| GET | `/api/diagnoses/{id}` | DOCTOR, PHARMACIST, ADMIN | Get diagnosis by ID |
| POST | `/api/diagnoses` | DOCTOR, PHARMACIST, ADMIN | Create diagnosis |
| PUT | `/api/diagnoses/{id}` | DOCTOR, PHARMACIST, ADMIN | Update diagnosis |
| DELETE | `/api/diagnoses/{id}` | DOCTOR, PHARMACIST, ADMIN | Delete diagnosis |
| GET | `/api/diagnoses/recipe/{recipeId}` | DOCTOR, PHARMACIST, ADMIN | Diagnoses for recipe |
| GET | `/api/diagnoses/customer/{customerId}` | DOCTOR, PHARMACIST, ADMIN | Diagnoses for customer |
| GET | `/api/diagnoses/search?name={name}` | DOCTOR, PHARMACIST, ADMIN | Name search |
| GET | `/api/diagnoses/icd10/{code}` | DOCTOR, PHARMACIST, ADMIN | ICD-10 search |
| GET | `/api/diagnoses/primary` | DOCTOR, PHARMACIST, ADMIN | Primary diagnoses only |
| GET | `/api/diagnoses/severity/{severity}` | DOCTOR, PHARMACIST, ADMIN | Filter by severity enum |
| GET | `/api/diagnoses/date-range?startDate={d1}&endDate={d2}` | DOCTOR, PHARMACIST, ADMIN | Date-range filter |
| GET | `/api/diagnoses/statistics/common` | DOCTOR, PHARMACIST, ADMIN | Aggregated diagnosis counts |

Create request example:

```json
{
  "recipe": { "id": 10 },
  "icd10Code": "J20.9",
  "name": "Acute bronchitis",
  "description": "Clinical diagnosis based on symptoms",
  "diagnosisDate": "2026-02-10",
  "isPrimary": true,
  "severity": "MODERATE",
  "notes": "Control in 5 days"
}
```

## 5.5 Sick Leaves (`/api/sick-leaves`)

| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/api/sick-leaves` | DOCTOR, PHARMACIST, ADMIN | List sick leaves |
| GET | `/api/sick-leaves/{id}` | DOCTOR, PHARMACIST, ADMIN | Get sick leave by ID |
| GET | `/api/sick-leaves/number/{leaveNumber}` | DOCTOR, PHARMACIST, ADMIN | Get by leave number |
| POST | `/api/sick-leaves` | DOCTOR, PHARMACIST, ADMIN | Create sick leave |
| PUT | `/api/sick-leaves/{id}` | DOCTOR, PHARMACIST, ADMIN | Update sick leave |
| DELETE | `/api/sick-leaves/{id}` | DOCTOR, PHARMACIST, ADMIN | Delete sick leave |
| GET | `/api/sick-leaves/customer/{customerId}` | DOCTOR, PHARMACIST, ADMIN | Sick leaves for customer |
| GET | `/api/sick-leaves/doctor/{doctorId}` | DOCTOR, PHARMACIST, ADMIN | Sick leaves by doctor |
| GET | `/api/sick-leaves/recipe/{recipeId}` | DOCTOR, PHARMACIST, ADMIN | Sick leaves for recipe |
| GET | `/api/sick-leaves/status/{status}` | DOCTOR, PHARMACIST, ADMIN | Filter by status |
| GET | `/api/sick-leaves/customer/{customerId}/active` | DOCTOR, PHARMACIST, ADMIN | Active sick leaves for customer |
| GET | `/api/sick-leaves/date-range?startDate={d1}&endDate={d2}` | DOCTOR, PHARMACIST, ADMIN | Date-range filter |
| POST | `/api/sick-leaves/{id}/extend` | DOCTOR, PHARMACIST, ADMIN | Extend leave |
| POST | `/api/sick-leaves/{id}/cancel` | DOCTOR, PHARMACIST, ADMIN | Cancel leave |
| POST | `/api/sick-leaves/{id}/complete` | DOCTOR, PHARMACIST, ADMIN | Mark complete |
| GET | `/api/sick-leaves/customer/{customerId}/check?date={date}` | DOCTOR, PHARMACIST, ADMIN | Check active on date |
| GET | `/api/sick-leaves/statistics/by-doctor` | DOCTOR, PHARMACIST, ADMIN | Count by doctor |
| GET | `/api/sick-leaves/statistics/by-month` | DOCTOR, PHARMACIST, ADMIN | Count by month |
| GET | `/api/sick-leaves/generate-number` | DOCTOR, PHARMACIST, ADMIN | Generate leave number |

Action payloads:

```json
{
  "additionalDays": 3,
  "reason": "Complications persist"
}
```

```json
{
  "reason": "Issued by mistake"
}
```

## 5.6 Reports (`/api/reports`)

| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/api/reports/patients-by-diagnosis?diagnosis={name}` | DOCTOR, PHARMACIST, ADMIN | Patients with diagnosis |
| GET | `/api/reports/common-diagnoses` | DOCTOR, PHARMACIST, ADMIN | Most common diagnoses |
| GET | `/api/reports/patients-by-primary-doctor/{doctorId}` | DOCTOR, PHARMACIST, ADMIN | Patients assigned to primary doctor |
| GET | `/api/reports/patient-count-by-primary-doctor` | DOCTOR, PHARMACIST, ADMIN | Patient count per primary doctor |
| GET | `/api/reports/visit-count-by-doctor` | DOCTOR, PHARMACIST, ADMIN | Examination count per doctor |
| GET | `/api/reports/patient-history/{customerId}` | DOCTOR, PHARMACIST, ADMIN | Full patient history (recipes) |
| GET | `/api/reports/examinations?startDate={d1}&endDate={d2}` | DOCTOR, PHARMACIST, ADMIN | Examinations in date range |
| GET | `/api/reports/doctor-examinations/{doctorId}?startDate={d1}&endDate={d2}` | DOCTOR, PHARMACIST, ADMIN | Examinations by doctor and period |
| GET | `/api/reports/sick-leaves-by-month` | DOCTOR, PHARMACIST, ADMIN | Monthly sick leave stats |
| GET | `/api/reports/doctors-sick-leave-ranking` | DOCTOR, PHARMACIST, ADMIN | Doctors ranked by sick leaves issued |
| GET | `/api/reports/valid-insurance` | DOCTOR, PHARMACIST, ADMIN | Customers with valid insurance |
| GET | `/api/reports/invalid-insurance` | DOCTOR, PHARMACIST, ADMIN | Customers without valid insurance |

Main report response DTOs:

```json
{
  "diagnosis": "Acute bronchitis",
  "count": 12
}
```

```json
{
  "doctorName": "Dr. John Smith",
  "count": 31
}
```

```json
{
  "year": 2026,
  "month": 2,
  "monthName": "February",
  "count": 9
}
```

## 6. Example Calls (cURL)

Get medicines:

```bash
curl -X GET "http://localhost:8084/api/medicines" \
  -H "Authorization: Bearer <JWT>"
```

Create customer:

```bash
curl -X POST "http://localhost:8084/api/customers" \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Maria Petrova",
    "age":29,
    "email":"maria.petrova@example.com",
    "phone":"+359888777666",
    "active":true
  }'
```

Get monthly sick leave stats:

```bash
curl -X GET "http://localhost:8084/api/reports/sick-leaves-by-month" \
  -H "Authorization: Bearer <JWT>"
```
